package com.leanforge.game.pending;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MemoryPendingGameRepository implements PendingGameRepository {

    private final Map<String, PendingGame> games = new ConcurrentHashMap<>();

    public PendingGame save(PendingGame pendingGame) {
        games.put(pendingGame.getChannelId(), pendingGame);
        return pendingGame;
    }

    public PendingGame findByChannelId(String channelId) {
        return games.get(channelId);
    }

    public void deleteByChannelId(String channelId) {
        games.remove(channelId);
    }

    public Stream<PendingGame> findAllByOrderByCreationDateAsc() {
        return games.values().stream();
    }

    public PendingGame findOne(String id) {
        return games.values().parallelStream()
                .filter(it -> id.equals(it.getId()))
                .findAny()
                .orElse(null);
    }
}
