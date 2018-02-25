package com.leanforge.game.pending;

import com.leanforge.game.queue.QueuedGame;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public interface PendingGameRepository {
    PendingGame save(PendingGame pendingGame);
    PendingGame findByChannelId(String channelId);
    void deleteByChannelId(String channelId);
    Stream<PendingGame> findAllByOrderByCreationDateAsc();
    PendingGame findOne(String id);
}
