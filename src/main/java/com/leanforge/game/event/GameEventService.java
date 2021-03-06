package com.leanforge.game.event;

import com.leanforge.game.event.model.GameAddedEvent;
import com.leanforge.game.event.model.GameEvent;
import com.leanforge.game.event.model.GameFinishedEvent;
import com.leanforge.game.event.model.GameStartedEvent;
import com.leanforge.game.queue.QueuedGame;
import com.leanforge.game.slack.SlackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class GameEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameEventService.class);

    private final Collection<GameEventObserver> observers = new CopyOnWriteArrayList<>();

    private final SlackService slackService;

    private final Integer timeout;

    @Autowired
    public GameEventService(SlackService slackService, @Value("${game.timeout}") Integer timeout) {
        this.slackService = slackService;
        this.timeout = timeout;
    }

    public GameEventObserver newObserver(Supplier<Boolean> isActive) {
        return new GameEventObserver(isActive);
    }

    public void emmit(GameEvent gameEvent) {
        LOGGER.debug("Firing event [{}] for game type {}, uuid: {}", gameEvent.getEventType(), gameEvent.getGameType(), gameEvent.getGameId());
        observers.forEach(observer -> observer.fire(gameEvent));
    }

    public void emmitGameFinished(QueuedGame game) {
        emmit(new GameFinishedEvent(toGameType(game.getChannelId()), game.getId(), slackIdsToNames(game.getPlayers()), game.getCreationDate(), game.getStartDate(), OffsetDateTime.now()));
    }

    public void emmitGameStarted(QueuedGame game) {
        emmit(createGameStartedEvent(game));
    }

    public void emmitGameAdded(QueuedGame game) {
        emmit(createGameAddedEvent(game));
    }

    public String toGameType(String slackChannelId) {
        return slackService.getChannelName(slackChannelId);
    }

    public GameEvent createGameStartedEvent(QueuedGame game) {
        OffsetDateTime timeout;
        if (game.getStartDate() == null) {
            timeout = OffsetDateTime.now().plusMinutes(this.timeout);
        } else {
            timeout = game.getStartDate().plusMinutes(this.timeout);
        }
        return new GameStartedEvent(toGameType(game.getChannelId()), game.getId(), slackIdsToNames(game.getPlayers()), game.getCreationDate(), game.getStartDate(), timeout);
    }

    public GameEvent createGameAddedEvent(QueuedGame game) {
        return new GameAddedEvent(toGameType(game.getChannelId()), game.getId(), slackIdsToNames(game.getPlayers()), game.getCreationDate());
    }

    private List<String> slackIdsToNames(List<String> ids) {
        return ids.stream()
                .map(slackService::getRealNameById)
                .collect(Collectors.toList());
    }

    public class GameEventObserver {
        private Consumer<GameEvent> eventHandler = it -> {};
        private final Supplier<Boolean> activeIndicator;

        public GameEventObserver(Supplier<Boolean> activeIndicator) {
            this.activeIndicator = activeIndicator;
            observers.add(this);
        }

        GameEventObserver onEvent(Consumer<GameEvent> eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }

        private void fire(GameEvent event) {
            if (!activeIndicator.get()) {
                observers.remove(this);
                return;
            }
            try {
                eventHandler.accept(event);
            } catch (RuntimeException e) {
                LOGGER.error("Can't fire event", e);
            }
        }
    }
}
