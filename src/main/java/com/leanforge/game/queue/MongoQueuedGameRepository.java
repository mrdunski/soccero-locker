package com.leanforge.game.queue;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoQueuedGameRepository extends MongoRepository<QueuedGame, String>, QueuedGameRepository {
}
