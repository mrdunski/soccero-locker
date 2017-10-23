package com.leanforge.game.event.model;

public class GameStartedEvent extends GameEvent {

    public GameStartedEvent(String gameType, String gameId) {
        setEventType("GAME_STARTED");
        setGameId(gameId);
        setGameType(gameType);
    }

    GameStartedEvent() {
    }
}
