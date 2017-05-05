package com.leanforge.game.message;

import com.leanforge.game.slack.SlackMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageBindingService {

    @Autowired
    MessageBindingRepository messageBindingRepository;

    public void bind(SlackMessage slackMessage, String id) {
        MessageBinding messageBinding = new MessageBinding();
        messageBinding.setBindId(id);
        messageBinding.setMessageUuid(uuid(slackMessage));
        messageBinding.setSlackMessageTimestamp(slackMessage.getTimestamp());
        messageBinding.setChannelId(slackMessage.getChannelId());

        messageBindingRepository.save(messageBinding);
    }

    public Optional<String> findBindingId(SlackMessage slackMessage) {
        return messageBindingRepository.findByMessageUuid(uuid(slackMessage))
                .map(MessageBinding::getBindId);
    }

    private String uuid(SlackMessage slackMessage) {
        return slackMessage.getChannelId() + '-' + slackMessage.getTimestamp();
    }

    public Optional<SlackMessage> findSlackMessage(String id) {
        return messageBindingRepository.findByBindId(id).map(MessageBinding::getSlackMessage);
    }
}
