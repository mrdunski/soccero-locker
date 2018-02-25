package com.leanforge.game.queue;

import java.util.stream.Stream;

public interface QueuedGameRepository {
    <S extends QueuedGame> S save(S queuedGame);
    Stream<QueuedGame> findAllByOrderByCreationDateAsc();
    void delete(String id);
    QueuedGame findOne(String id);
}
