package com.leanforge.game.pending;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoPendingGameRepository extends MongoRepository<PendingGame, String>, PendingGameRepository {
}
