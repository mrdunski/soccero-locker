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
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class GameEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameEventService.class);

    private final Collection<GameEventObserver> observers = new CopyOnWriteArrayList<>();

    private final SlackService slackService;

    @Autowired
    public GameEventService(SlackService slackService) {
        this.slackService = slackService;
    }

    public GameEventObserver newObserver(Supplier<Boolean> isActive) {
        return new GameEventObserver(isActive);
    }

    public void emmit(GameEvent gameEvent) {
        LOGGER.debug("Firing event [{}] for game type {}, uuid: {}", gameEvent.getEventType(), gameEvent.getGameType(), gameEvent.getGameId());
        observers.forEach(observer -> observer.fire(gameEvent));
    }

    public void emmitGameFinished(QueuedGame game) {
        emmit(new GameFinishedEvent(toGameType(game.getChannelId()), game.getId()));
    }

    public void emmitGameStarted(QueuedGame game) {
        emmit(new GameStartedEvent(toGameType(game.getChannelId()), game.getId()));
    }

    public void emmitGameAdded(QueuedGame game) {
        emmit(new GameAddedEvent(toGameType(game.getChannelId()), game.getId()));
    }

    public String toGameType(String slackChannelId) {
        return slackService.getChannelName(slackChannelId);
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
