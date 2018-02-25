package com.leanforge.game.message;

import java.util.Optional;

public interface MessageBindingRepository {
    MessageBinding save(MessageBinding messageBinding);
    Optional<MessageBinding> findByMessageUuid(String messageUuid);
    Optional<MessageBinding> findByBindId(String id);
}
