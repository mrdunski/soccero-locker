package com.leanforge.game.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class QueuedGameService {

    private final QueuedGameRepository repository;

    @Autowired
    public QueuedGameService(QueuedGameRepository repository) {
        this.repository = repository;
    }

    public QueuedGame scheduleGame(String channelId, String creatorId, int priority, List<String> players, String comment, OffsetDateTime postponeDate) {
        QueuedGame game = new QueuedGame();
        game.setCreatorId(creatorId);
        game.setCreationDate(OffsetDateTime.now());
        game.setChannelId(channelId);
        game.setPriority(priority);
        game.setComment(comment);
        game.setPlayers(players);
        game.setPostponeDate(postponeDate);

        if (postponeDate == null && !findStartedGame().isPresent()) {
            game.setStartDate(OffsetDateTime.now());
        }

        repository.save(game);

        return game;
    }

    public Stream<QueuedGame> scheduledGames() {
        return repository.findAllByOrderByCreationDateAsc();
    }

    public synchronized void endGame(String id) {
        repository.delete(id);
    }

    public synchronized Optional<QueuedGame> startTopGame() {
        return findTopPriorityGame()
                .flatMap(this::startGame);
    }

    public synchronized Optional<QueuedGame> startGame(String id) {
        return Optional.ofNullable(repository.findOne(id))
                .flatMap(this::startGame);
    }

    private Optional<QueuedGame> startGame(QueuedGame game) {
        if (findStartedGame().isPresent()) {
            return Optional.empty();
        }

        game.setStartDate(OffsetDateTime.now());
        repository.save(game);
        return Optional.of(game);
    }

    public Optional<QueuedGame> findStartedGame() {
        return repository.findAllByOrderByCreationDateAsc()
                .filter(it -> it.getStartDate() != null)
                .min(Comparator.comparing(QueuedGame::getStartDate));
    }

    public Optional<QueuedGame> findOldestGame() {
        return repository.findAllByOrderByCreationDateAsc()
                .findFirst();
    }

    private Optional<QueuedGame> findTopPriorityGame() {
        OffsetDateTime now = OffsetDateTime.now();
        return repository.findAllByOrderByCreationDateAsc()
                .filter(it -> it.getStartDate() == null)
                .filter(it -> it.getPostponeDate() == null || it.getPostponeDate().isBefore(now))
                .min(
                        Comparator.comparing(QueuedGame::getPriority)
                                .thenComparing(QueuedGame::getCreationDate)
                );
    }

    public Optional<QueuedGame> find(String gameId) {
        return Optional.ofNullable(repository.findOne(gameId));
    }

    public boolean isQueueEmpty() {
        return repository.count() == 0;
    }
}
