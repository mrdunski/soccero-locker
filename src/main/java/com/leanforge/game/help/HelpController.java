package com.leanforge.game.help;

import com.leanforge.game.slack.SlackService;
import com.leanforge.game.slack.listener.SlackChannelId;
import com.leanforge.game.slack.listener.SlackController;
import com.leanforge.game.slack.listener.SlackMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@SlackController
public class HelpController {

    @Autowired
    SlackService slackService;

    @SlackMessageListener("help")
    public void helpMe(@SlackChannelId String channelId) {
        slackService.sendChannelMessage(channelId, generateHelpMessage(), "sun_with_face");
    }

    private String generateHelpMessage() {
        return "Features and commands:\n" +
                ":pushpin: `help` - prints help message.\n" +
                ":pushpin: `findPlayers` - search for players in 4-players game. You are included. If 3 players will join, then new game will be added to the queue.\n" +
                ":pushpin: `findPlayers n` - search for players in n-players game.\n" +
                ":pushpin: `findFoosballPlayers` - search for players for foosball game\n" +
                ":pushpin: `startGame` - add game directly to the queue.\n" +
                ":pushpin: `queueStatus` - print current queue status.\n" +
                ":pushpin: (:x:) on `go go go` message - end the game and informs players in queue.\n" +
                ":pushpin: (:heavy_plus_sign:) on pending game message - add/remove yourself to the game.\n" +
                ":pushpin: `+ @player1 @player2 ...` - add somebody to pending game \n" +
                ":pushpin: `- @player1 @player2 ...` - remove somebody\n" +
                ":pushpin: (:fast_forward:) on pending game message - force start pending game\n" +
                ":pushpin: `forceStart` on pending game message thread - force start pending game\n" +
                ":pushpin: Game status page: http://game-status.playroom.leanforge.pl";
    }
}
