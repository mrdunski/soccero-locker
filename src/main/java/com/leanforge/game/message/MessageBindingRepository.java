package com.leanforge.game.message;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MessageBindingRepository {

    private final Map<String, MessageBinding> bindings = new ConcurrentHashMap<>();

    public void save(MessageBinding messageBinding) {
        bindings.put(messageBinding.getMessageUuid(), messageBinding);
    }

    public Optional<MessageBinding> findByMessageUuid(String messageUuid) {
        return Optional.ofNullable(bindings.get(messageUuid));
    }

    public Optional<MessageBinding> findByBindId(String id) {
        return bindings.values()
                .parallelStream()
                .filter(it -> id.equals(it.getBindId()))
                .findAny();
    }
}
