package com.leanforge.game.event.model;

public class GameAddedEvent extends GameEvent {

    GameAddedEvent() {
    }

    public GameAddedEvent(String gameType, String gameId) {
        setEventType("GAME_ADDED");
        setGameId(gameId);
        setGameType(gameType);
    }
}
