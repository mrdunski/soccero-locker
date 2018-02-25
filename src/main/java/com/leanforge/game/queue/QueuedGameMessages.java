package com.leanforge.game.queue;

import com.leanforge.game.slack.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class QueuedGameMessages {

    @Autowired
    SlackService slackService;

    public String statusMessage(QueuedGame game) {
        ZoneId userTimezone = slackService.getUserTimezone(game.getCreatorId());

        String name = slackService.getRealNameById(game.getCreatorId());
        String channel = slackService.getChannelName(game.getChannelId());
        String addedOn = DateTimeFormatter.ISO_TIME.format(OffsetDateTime.from(game.getCreationDate()).atZoneSameInstant(userTimezone));

        if (game.getStartDate() != null) {
            String startedOn = DateTimeFormatter.ISO_TIME.format(OffsetDateTime.from(game.getStartDate()).atZoneSameInstant(userTimezone));
            return String.format(":video_game: %s (added by %s at %s), started at %s", channel, name, addedOn, startedOn);
        }

        return String.format(":video_game: %s (added by %s at %s) p%s", channel, name, addedOn, game.getPriority());
    }

}
