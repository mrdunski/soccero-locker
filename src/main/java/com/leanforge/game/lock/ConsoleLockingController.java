package com.leanforge.game.lock;

import com.leanforge.game.pending.PendingGame;
import com.leanforge.game.slack.SlackMessage;
import com.leanforge.game.slack.listener.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.leanforge.game.slack.listener.SlackReactionListener.Action.ADD;
import static com.leanforge.game.slack.listener.SlackReactionListener.Action.REMOVE;

@SlackController
public class ConsoleLockingController {

    private static final Pattern slackUserIds = Pattern.compile("<@([^>]+)>");

    @Autowired
    ConsoleLockingService consoleLockingService;

    @SlackMessageListener("startGame")
    public void startGame(SlackMessage message) {
        consoleLockingService.startGame(message);
    }

    @SlackMessageListener("startGame p(\\d{1}) `(.*)`")
    public void startPriorityGame(SlackMessage message, @SlackMessageRegexGroup(1) String priorityString, @SlackMessageRegexGroup(2) String comment) {
        int priority = Integer.parseInt(priorityString);
        consoleLockingService.startGame(message, priority, comment);
    }

    @SlackMessageListener("queueStatus")
    public void queueStatus(SlackMessage message) {
        consoleLockingService.printQueueStatus(message);
    }

    @SlackReactionListener("x")
    public void endGame(SlackMessage message) {
        consoleLockingService.endGame(message);
    }

    @SlackReactionListener("rewind")
    public void postponeGame(@SlackUserId String userId, SlackMessage message) {
        consoleLockingService.postponeGame(userId, message);
    }

    @SlackReactionListener(value = "heavy_plus_sign", action = ADD)
    public void addPlayer(SlackMessage message, @SlackUserId String userId) {
        consoleLockingService.addPlayer(message, userId);
    }

    @SlackReactionListener(value = "heavy_plus_sign", action = REMOVE)
    public void removePlayer(SlackMessage message, @SlackUserId String userId) {
        consoleLockingService.removePlayer(message, userId);
    }

    @SlackReactionListener(value = "fast_forward", action = ADD)
    public void forceStartReaction(SlackMessage message) {
        consoleLockingService.forceStartPendingGame(message);
    }

    @SlackThreadMessageListener(value = "forceStart")
    public void forceStartMessage(SlackMessage message) {
        consoleLockingService.forceStartPendingGame(message);
    }

    @SlackMessageListener("findPlayers")
    public void findPlayers(SlackMessage message) {
        consoleLockingService.findPlayers(message);
    }

    @SlackMessageListener("findFoosballPlayers")
    public void findFoosballPlayers(SlackMessage message) {
        consoleLockingService.findPlayers(message, 4, PendingGame.GameType.FOOSBALL);
    }

    @SlackMessageListener("findPlayers (\\d+)")
    public void findPlayers(SlackMessage message, @SlackMessageRegexGroup(1) String playersCount) {
        consoleLockingService.findPlayers(message, Integer.parseInt(playersCount));
    }

    @SlackMessageListener("\\+ .+")
    public void bulkAddPlayers(SlackMessage message, @SlackMessageContent String content) {
        consoleLockingService.addPlayers(message, extractUserIds(content));
    }

    @SlackMessageListener("- .+")
    public void bulkRemovePlayers(SlackMessage message, @SlackMessageContent String content) {
        consoleLockingService.removePlayers(message, extractUserIds(content));
    }

    private List<String> extractUserIds(String message) {
        Matcher matcher = slackUserIds.matcher(message);
        ArrayList<String> ids = new ArrayList<>();

        while (matcher.find()) {
            ids.add(matcher.group(1));
        }

        return ids;
    }
}
