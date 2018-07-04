package com.leanforge.game.event.model;

import java.util.List;

public class GameAddedEvent extends GameEvent {

    GameAddedEvent() {
    }

    public GameAddedEvent(String gameType, String gameId, List<String> players) {
        setEventType("GAME_ADDED");
        setGameId(gameId);
        setGameType(gameType);
        setPlayers(players);
    }
}
