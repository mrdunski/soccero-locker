package com.leanforge.game.queue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Predicate;

public class QueuedGame {

    public static Predicate<QueuedGame> startedBefore(int time, ChronoUnit unit) {
        return game -> game.getStartDate() != null && game.getStartDate().isBefore(OffsetDateTime.now().minus(time, unit));
    }

    @Id
    private String id = UUID.randomUUID().toString();
    private String creatorId;
    private String channelId;
    private OffsetDateTime creationDate;
    private OffsetDateTime startDate;
    private int priority = 5;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isStarted() {
        return startDate != null;
    }
}
