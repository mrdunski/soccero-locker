package com.leanforge.game.queue;

import java.util.stream.Stream;

public interface QueuedGameRepository {
    <S extends QueuedGame> S save(S queuedGame);
    Stream<QueuedGame> findAllByOrderByCreationDateAsc();

    long count();
    void delete(String id);
    QueuedGame findOne(String id);
}
