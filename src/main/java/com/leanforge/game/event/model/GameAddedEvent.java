package com.leanforge.game.event.model;

import java.time.OffsetDateTime;
import java.util.List;

public class GameAddedEvent extends GameEvent {

    private OffsetDateTime creationDate;

    GameAddedEvent() {
    }

    public GameAddedEvent(String gameType, String gameId, List<String> players, OffsetDateTime creationDate) {
        setEventType("GAME_ADDED");
        setGameId(gameId);
        setGameType(gameType);
        setPlayers(players);
        setCreationDate(creationDate);
    }


    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
