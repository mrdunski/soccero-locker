package com.leanforge.game.event.model;

import java.time.OffsetDateTime;
import java.util.List;

public class GameStartedEvent extends GameEvent {

    private OffsetDateTime creationDate;
    private OffsetDateTime startDate;
    private OffsetDateTime timeoutDate;

    public GameStartedEvent(String gameType, String gameId, List<String> players, OffsetDateTime creationDate, OffsetDateTime startDate, OffsetDateTime timeoutDate) {
        setEventType("GAME_STARTED");
        setGameId(gameId);
        setGameType(gameType);
        setPlayers(players);
        setCreationDate(creationDate);
        setStartDate(startDate);
        setTimeoutDate(timeoutDate);
    }

    GameStartedEvent() {
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

    public OffsetDateTime getTimeoutDate() {
        return timeoutDate;
    }

    public void setTimeoutDate(OffsetDateTime timeoutDate) {
        this.timeoutDate = timeoutDate;
    }
}
