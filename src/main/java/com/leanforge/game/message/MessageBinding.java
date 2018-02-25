package com.leanforge.game.message;

import com.leanforge.game.slack.SlackMessage;
import org.springframework.data.annotation.Id;

public class MessageBinding {

    @Id
    private String messageUuid;
    private String bindId;
    private String slackMessageTimestamp;
    private String channelId;

    public String getMessageUuid() {
        return messageUuid;
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }

    public String getSlackMessageTimestamp() {
        return slackMessageTimestamp;
    }

    public void setSlackMessageTimestamp(String slackMessageTimestamp) {
        this.slackMessageTimestamp = slackMessageTimestamp;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public SlackMessage getSlackMessage() {
        return new SlackMessage(slackMessageTimestamp, channelId, null);
    }
}
