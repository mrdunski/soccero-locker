package com.leanforge.game.event.model;

import java.util.List;

public class GameStartedEvent extends GameEvent {

    public GameStartedEvent(String gameType, String gameId, List<String> players) {
        setEventType("GAME_STARTED");
        setGameId(gameId);
        setGameType(gameType);
        setPlayers(players);
    }

    GameStartedEvent() {
    }
}
