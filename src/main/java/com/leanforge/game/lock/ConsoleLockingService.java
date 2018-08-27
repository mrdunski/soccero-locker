package com.leanforge.game.lock;

import com.leanforge.game.event.GameEventService;
import com.leanforge.game.message.MessageBindingService;
import com.leanforge.game.pending.PendingGame;
import com.leanforge.game.pending.PendingGameMessages;
import com.leanforge.game.pending.PendingGameService;
import com.leanforge.game.queue.QueuedGame;
import com.leanforge.game.queue.QueuedGameMessages;
import com.leanforge.game.queue.QueuedGameService;
import com.leanforge.game.slack.SlackMessage;
import com.leanforge.game.slack.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ConsoleLockingService {

    private static final String GO_MESSAGE_TEMPLATE = "%s go go go! (end game with :x:)%s";
    private static final String HERE = "<!here|@here>";
    public static final String GAME_FINISHED_MESSAGE = ":horse_racing: Game finished in: ";
    public static final String GAME_POSTPONED_MESSAGE = ":blowfish: Game was postponed by: ";
    public static final String WAIT_MESSAGE = ":horse: Hold your horses. Current queue: \n";

    private final QueuedGameService queuedGameService;
    private final QueuedGameMessages queuedGameMessages;
    private final PendingGameService pendingGameService;
    private final PendingGameMessages pendingGameMessages;
    private final MessageBindingService messageBindingService;
    private final SlackService slackService;
    private final GameEventService gameEventService;
    private final Integer timeout;

    @Autowired
    public ConsoleLockingService(QueuedGameService queuedGameService,
                                 QueuedGameMessages queuedGameMessages,
                                 PendingGameService pendingGameService,
                                 PendingGameMessages pendingGameMessages,
                                 MessageBindingService messageBindingService,
                                 SlackService slackService,
                                 GameEventService gameEventService,
                                 @Value("${game.timeout}") Integer timeout) {
        this.queuedGameService = queuedGameService;
        this.queuedGameMessages = queuedGameMessages;
        this.pendingGameService = pendingGameService;
        this.pendingGameMessages = pendingGameMessages;
        this.messageBindingService = messageBindingService;
        this.slackService = slackService;
        this.gameEventService = gameEventService;
        this.timeout = timeout;
    }

    @Scheduled(fixedDelay = 1000)
    public synchronized void removeOldGames() {
        Optional<QueuedGame> startedGame = queuedGameService.findStartedGame();
        startedGame
                .filter(QueuedGame.startedBefore(timeout, ChronoUnit.MINUTES))
                .ifPresent( game -> {
                    endGameAndMoveQueueUp(game);
                    slackService.sendChannelMessage(game.getChannelId(), ":turtle: " + creatorNotifier(game) + " your game has been ended by timeout!");
                });
        if (!startedGame.isPresent() && !queuedGameService.isQueueEmpty()) {
            moveQueueUp();
            updatePendingGames();
        }
    }

    private String creatorNotifier(QueuedGame game) {
        return String.format("<@%s>", game.getCreatorId());
    }

    public void startGame(SlackMessage slackMessage) {
        startGame(slackMessage, 5, null);
    }

    public void startGame(SlackMessage slackMessage, int priority, String comment) {
        startGame(slackMessage.getChannelId(), slackMessage.getSenderId(), priority, Collections.emptyList(), comment, null);
        updatePendingGames();
    }

    public void forceStartPendingGame(SlackMessage gamePointer) {
        messageBindingService.findBindingId(gamePointer)
                .flatMap(pendingGameService::find)
                .ifPresent(pendingGame -> {
                    pendingGameService.closePendingGameWithExistingPlayers(pendingGame);
                    startGame(pendingGame);
                    removePlayerFromAllGames(pendingGame.getPlayerIds());
                });

        updatePendingGames();
    }

    public void endGame(SlackMessage gamePointer) {
        messageBindingService.findBindingId(gamePointer)
                .flatMap(queuedGameService::find)
                .ifPresent(game -> endGameAndUpdateMessage(gamePointer, game));
    }

    public void postponeGame(String userId, SlackMessage gamePointer) {
        messageBindingService.findBindingId(gamePointer)
                .flatMap(queuedGameService::find)
                .ifPresent(game -> postponeGame(userId, gamePointer, game));
    }

    private void postponeGame(String userId, SlackMessage gamePointer, QueuedGame game) {
        ZoneId userTimezone = slackService.getUserTimezone(userId);
        queuedGameService.endGame(game.getId());
        gameEventService.emmitGameFinished(game);
        slackService.updateMessage(gamePointer, GAME_POSTPONED_MESSAGE + "<@" + userId + ">");
        OffsetDateTime postpone = OffsetDateTime
                .now()
                .plusMinutes(5)
                .withSecond(0)
                .withNano(0)
                .atZoneSameInstant(userTimezone)
                .toOffsetDateTime();
        startGame(game.getChannelId(), game.getCreatorId(), game.getPriority(), game.getPlayers(), game.getComment(), postpone);
        updatePendingGames();
    }

    public void printQueueStatus(SlackMessage slackMessage) {
        slackService.sendChannelMessage(slackMessage.getChannelId(), "Current queue:\n" + queueStatus());
    }

    public PendingGame findPlayers(SlackMessage slackMessage, int players, PendingGame.GameType gameType) {
        Stream<PendingGame> gameStream = pendingGameService.allPendingGames();
        PendingGame pendingGame = pendingGameService.addPendingGame(slackMessage.getChannelId(), slackMessage.getSenderId(), players, gameType);
        SlackMessage pendingGameMarker = slackService.sendChannelMessage(slackMessage.getChannelId(), pendingGameMessages.statusMessage(pendingGame), "heavy_plus_sign", "fast_forward");
        messageBindingService.bind(pendingGameMarker, pendingGame.getId());

        updatePendingGames(gameStream);
        return pendingGame;
    }

    public PendingGame findPlayers(SlackMessage slackMessage) {
        return findPlayers(slackMessage, 4);
    }

    public PendingGame findPlayers(SlackMessage slackMessage, int players) {
        return findPlayers(slackMessage, players, PendingGame.GameType.CONSOLE);
    }

    public void addPlayer(SlackMessage gamePointer, String userId) {
        messageBindingService.findBindingId(gamePointer)
                .flatMap(pendingGameService::find)
                .ifPresent(pendingGame -> {
                    pendingGameService.addPlayers(pendingGame, userId);
                    if (pendingGame.missingPlayers() == 0) {
                        startGame(pendingGame);
                        removePlayerFromAllGames(pendingGame.getPlayerIds());
                    }
                });

        updatePendingGames();
    }

    public void removePlayer(SlackMessage gamePointer, String userId) {
        messageBindingService.findBindingId(gamePointer)
                .flatMap(pendingGameService::find)
                .ifPresent(pendingGame -> pendingGameService.removePlayers(pendingGame, userId));

        updatePendingGames();
    }

    public void addPlayers(SlackMessage slackMessage, List<String> playerIds) {
        PendingGame pendingGame = pendingGameService.findByChannelId(slackMessage.getChannelId())
                .orElseGet(() -> findPlayers(slackMessage));
        pendingGameService.addPlayers(pendingGame, playerIds.toArray(new String[0]));
        if (pendingGame.missingPlayers() == 0) {
            startGame(pendingGame);
            removePlayerFromAllGames(pendingGame.getPlayerIds());
        }


        updatePendingGames();
    }

    private void removePlayerFromAllGames(Collection<String> playerIds) {
        Set<String> playerSet = new HashSet<>(playerIds);
        String[] playerArray = playerIds.toArray(new String[playerIds.size()]);
        pendingGameService.allPendingGames()
                .filter(it -> it.getPlayerIds().stream().anyMatch(playerSet::contains))
                .forEach(it -> pendingGameService.removePlayers(it, playerArray));
    }

    public void removePlayers(SlackMessage slackMessage, List<String> playerIds) {
        pendingGameService.findByChannelId(slackMessage.getChannelId())
                .ifPresent(pendingGame -> {
                    pendingGameService.removePlayers(pendingGame, playerIds.toArray(new String[0]));
                });
        updatePendingGames();
    }

    private void updatePendingGames() {
        updatePendingGames(pendingGameService.allPendingGames());
    }

    private void updatePendingGames(Stream<PendingGame> gameStream) {
        gameStream
                .parallel()
                .forEach(this::update);
    }

    private void endGameAndUpdateMessage(SlackMessage gamePointer, QueuedGame game) {
        slackService.updateMessage(gamePointer, GAME_FINISHED_MESSAGE + Duration.between(game.getStartDate(), OffsetDateTime.now()));
        slackService.addReactions(gamePointer, "ok_hand");
        endGameAndMoveQueueUp(game);
    }

    private void endGameAndMoveQueueUp(QueuedGame game) {
        gameEventService.emmitGameFinished(game);
        queuedGameService.endGame(game.getId());
        moveQueueUp();
        updatePendingGames();
    }

    private void startGame(PendingGame game) {
        pendingGameService.delete(game.getChannelId());

        switch (game.getGameType()) {
            case CONSOLE:
                startConsoleGame(game);
                break;
            case FOOSBALL:
                startFoosballGame(game);
                break;
        }

        update(game);
        removePlayerFromAllGames(game.getPlayerIds());
    }

    private void startConsoleGame(PendingGame game) {
        startGame(game.getChannelId(), game.getCreatorId(), 5, game.getPlayerIds(), null, null);
    }

    private void startFoosballGame(PendingGame game) {
        slackService.sendChannelMessage(game.getChannelId(), pendingGameMessages.createFoosballGameMessage(game));
    }

    private void startGame(String channelId, String creatorId, int priority, List<String> players, String comments, OffsetDateTime postponeDate) {
        QueuedGame game = queuedGameService.scheduleGame(channelId, creatorId, priority, players, comments, postponeDate);
        gameEventService.emmitGameAdded(game);
        if (game.isStarted()) {
            SlackMessage statusMessage = slackService.sendChannelMessage(channelId, goGameMessage(game), "x", "rewind");
            messageBindingService.bind(statusMessage, game.getId());
            gameEventService.emmitGameStarted(game);
        } else {
            slackService.sendChannelMessage(channelId, WAIT_MESSAGE + queueStatus());
        }
    }

    private void moveQueueUp() {
        queuedGameService.startTopGame().ifPresent(game -> {
            SlackMessage statusMessage = slackService.sendChannelMessage(game.getChannelId(), goGameMessage(game), "x", "rewind");
            messageBindingService.bind(statusMessage, game.getId());
            gameEventService.emmitGameStarted(game);
        });
    }

    private String queueStatus() {
        return queuedGameService
                .scheduledGames()
                .map(queuedGameMessages::statusMessage)
                .collect(Collectors.joining("\n> ", "> ", ""));
    }

    private void update(PendingGame pendingGame) {
        messageBindingService.findSlackMessage(pendingGame.getId())
                .ifPresent(slackMessage -> {
                    slackService.updateMessage(slackMessage, pendingGameMessages.statusMessage(pendingGame));
                });
    }

    String goGameMessage(QueuedGame queuedGame) {
        String keywords = HERE;
        if (!queuedGame.getPlayers().isEmpty()) {
            keywords = queuedGame.getPlayers().stream().map(it -> "<@" + it + ">").collect(Collectors.joining(" "));
        }

        String comment = "";

        if (queuedGame.getComment() != null) {
            comment = " - `" + queuedGame.getComment() + "`";
        }
        return String.format(GO_MESSAGE_TEMPLATE, keywords, comment);
    }
}
