package com.leanforge.game.pending;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
public class PendingGameRepository {

    private final Map<String, PendingGame> games = new ConcurrentHashMap<>();

    public void save(PendingGame pendingGame) {
        games.put(pendingGame.getChannelId(), pendingGame);
    }

    public Optional<PendingGame> findByChannelId(String channelId) {
        return Optional.ofNullable(games.get(channelId));
    }

    public void delete(String channelId) {
        games.remove(channelId);
    }

    public Stream<PendingGame> findAll() {
        return games.values().stream();
    }

    public Optional<PendingGame> find(String id) {
        return games.values().parallelStream()
                .filter(it -> id.equals(it.getId()))
                .findAny();
    }
}
