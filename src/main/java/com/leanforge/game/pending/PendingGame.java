package com.leanforge.game.pending;

import org.springframework.data.annotation.Id;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PendingGame {
    @Id
    private String id = UUID.randomUUID().toString();
    private String channelId;
    private String creatorId;
    private int playerCount;
    private List<String> playerIds;
    private OffsetDateTime creationDate = OffsetDateTime.now();
    private GameType gameType = GameType.CONSOLE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public int missingPlayers() {
        return playerCount - playerIds.size();
    }

    public enum GameType {
        CONSOLE,FOOSBALL
    }
}
