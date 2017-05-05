package com.leanforge.game.pending;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PendingGameService {

    private final PendingGameRepository pendingGameRepository;

    @Autowired
    public PendingGameService(PendingGameRepository pendingGameRepository) {
        this.pendingGameRepository = pendingGameRepository;
    }

    public Optional<PendingGame> find(String id) {
        return pendingGameRepository.find(id);
    }

    public Optional<PendingGame> findByChannelId(String channelId) {
        return pendingGameRepository.findByChannelId(channelId);
    }

    public PendingGame addPendingGame(String channelId, String creatorId, int players) {
        PendingGame game = create(channelId, creatorId, players);

        pendingGameRepository.save(game);

        return game;
    }

    private PendingGame create(String channelId, String creatorId, int players) {
        PendingGame game = new PendingGame();
        game.setChannelId(channelId);
        game.setCreationDate(OffsetDateTime.now());
        game.setPlayerCount(players);
        game.setCreatorId(creatorId);
        game.setPlayerIds(Collections.singletonList(creatorId));

        return game;
    }

    public Stream<PendingGame> allPendingGames() {
        return pendingGameRepository.findAll();
    }

    public synchronized void addPlayers(PendingGame pendingGame, String... playerIds) {
        if (playerIds.length == 0) {
            throw new IllegalArgumentException("No players to add");
        }
        List<String> players = Stream.concat(pendingGame.getPlayerIds().stream(), Stream.of(playerIds))
                .distinct()
                .limit(pendingGame.getPlayerCount())
                .collect(Collectors.toList());

        pendingGame.setPlayerIds(players);

        pendingGameRepository.save(pendingGame);
    }

    public synchronized void removePlayers(PendingGame pendingGame, String... playerIds) {
        if (playerIds.length == 0) {
            throw new IllegalArgumentException("No players to remove");
        }

        List<String> players = new ArrayList<>(pendingGame.getPlayerIds());
        players.removeAll(Arrays.asList(playerIds));
        pendingGame.setPlayerIds(players);
        pendingGameRepository.save(pendingGame);
    }

    public void delete(String channelId) {
        pendingGameRepository.delete(channelId);
    }
}
