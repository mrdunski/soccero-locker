package com.leanforge.game.event.model;

public class GameFinishedEvent extends GameEvent {

    public GameFinishedEvent(String gameType, String gameId) {
        setEventType("GAME_FINISHED");
        setGameId(gameId);
        setGameType(gameType);
    }

    GameFinishedEvent() {
    }
}
