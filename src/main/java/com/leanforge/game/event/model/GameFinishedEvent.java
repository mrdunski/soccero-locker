package com.leanforge.game.event.model;

import java.util.List;

public class GameFinishedEvent extends GameEvent {

    public GameFinishedEvent(String gameType, String gameId, List<String> players) {
        setEventType("GAME_FINISHED");
        setGameId(gameId);
        setGameType(gameType);
        setPlayers(players);
    }

    GameFinishedEvent() {
    }
}
