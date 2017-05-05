package com.leanforge.game.queue;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

@Repository
public class QueuedGameRepository {

    private final Collection<QueuedGame> games = new CopyOnWriteArraySet<>();

    public void save(QueuedGame queuedGame) {
        games.add(queuedGame);
    }

    public Stream<QueuedGame> findAllOrderedByCreationDateAsc() {
        return games.stream().sorted(Comparator.comparing(QueuedGame::getCreationDate));
    }

    public void delete(String id) {
        games.parallelStream()
                .filter(it -> it.getId().equals(id))
                .findAny()
                .ifPresent(games::remove);
    }

    public Optional<QueuedGame> find(String id) {
        return games.parallelStream()
                .filter(it -> id.equals(it.getId()))
                .findAny();
    }
}
