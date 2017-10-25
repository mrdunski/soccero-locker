package com.leanforge.game.pending;

import com.leanforge.game.queue.QueuedGameService;
import com.leanforge.game.slack.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class PendingGameMessages {
    public static final String CREATE_FOOSBALL_GAME_COMMAND = "<@%s> could you create foosball game? Here are the players: \n";

    @Autowired
    SlackService slackService;

    @Autowired
    QueuedGameService queuedGameService;

    @Autowired
    PendingGameService pendingGameService;

    @Value("${foosball.user}")
    String createFoosUserId;

    public String createFoosballGameMessage(PendingGame game) {
        return String.format(CREATE_FOOSBALL_GAME_COMMAND, createFoosUserId) + game
                .getPlayerIds()
                .stream()
                .map(id -> ":joystick: <@" + id + ">")
                .collect(Collectors.joining("\n"));
    }

    public String statusMessage(PendingGame pendingGame) {
        if (pendingGame.missingPlayers() == 0) {
            return "Done.\nPlayers: \n" + playerList(pendingGame.getPlayerIds());
        }
        return findPlayersMessage(pendingGame.getPlayerIds(), pendingGame.getPlayerCount())
                + otherPendingGamesFor(pendingGame).orElse("")
                + gamesInQueueMessage().orElse("");
    }

    private Optional<String> gamesInQueueMessage() {
        long count = queuedGameService.scheduledGames().count();
        if (count == 0) {
            return Optional.empty();
        }

        return Optional.of("\n\nGames in queue: \n> " + count);
    }

    private Optional<String> otherPendingGamesFor(PendingGame skip) {
        if (pendingGameService.allPendingGames().count() == 1) {
            return Optional.empty();
        }
        return Optional.of(pendingGameService.allPendingGames()
                .sorted(Comparator.comparing(PendingGame::missingPlayers))
                .filter(Predicate.isEqual(skip).negate())
                .map(it -> "> :video_game: " + slackService.getChannelName(it.getChannelId()) + ", missing players: " + (it.missingPlayers()))
                .collect(Collectors.joining("\n", "\n\nPending Games: \n", "\n")));
    }

    private String findPlayersMessage(List<String> playerList, int playerCount) {
        int needed = playerCount - playerList.size();
        return "<!here|@here> " + needed + "? Click :heavy_plus_sign: \nPlayers: \n" + playerList(playerList);
    }

    private String playerList(List<String> playerIds) {
        return playerIds.stream()
                .map(slackService::getRealNameById)
                .map(it -> "> :joystick: " + it)
                .collect(Collectors.joining("\n"));
    }

}
