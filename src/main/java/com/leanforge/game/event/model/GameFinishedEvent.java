package com.leanforge.game.event.model;

import java.time.OffsetDateTime;
import java.util.List;

public class GameFinishedEvent extends GameEvent {

    private OffsetDateTime creationDate;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    public GameFinishedEvent(String gameType, String gameId, List<String> players, OffsetDateTime creationDate, OffsetDateTime startDate, OffsetDateTime endDate) {
        setEventType("GAME_FINISHED");
        setGameId(gameId);
        setGameType(gameType);
        setPlayers(players);
        setStartDate(startDate);
        setCreationDate(creationDate);
        setEndDate(endDate);
    }

    GameFinishedEvent() {
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

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }
}
