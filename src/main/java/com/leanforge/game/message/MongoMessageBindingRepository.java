package com.leanforge.game.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoMessageBindingRepository extends MessageBindingRepository, MongoRepository<MessageBinding, String> {
}
