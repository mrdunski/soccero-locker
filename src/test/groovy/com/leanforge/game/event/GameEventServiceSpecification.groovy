package com.leanforge.game.event

import com.leanforge.game.event.model.GameEvent
import com.leanforge.game.queue.QueuedGame
import com.leanforge.game.slack.SlackService
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer
import java.util.function.Supplier

class GameEventServiceSpecification extends Specification {

    SlackService slackService = Stub(SlackService) {
        getChannelName(_) >> {
            return it[0] + '-name'
        }
    }

    @Subject
    GameEventService gameEventService = new GameEventService(slackService, 30)

    def "should inform all active observers"() {
        given:
        Consumer<GameEvent> consumerA = Mock(Consumer)
        Consumer<GameEvent> consumerB = Mock(Consumer)
        Consumer<GameEvent> inactiveConsumer = Mock(Consumer)
        GameEvent gameEvent = Mock(GameEvent)

        when:
        gameEventService.newObserver({true}).onEvent(consumerA)
        gameEventService.newObserver({true}).onEvent(consumerB)
        gameEventService.newObserver({false}).onEvent(inactiveConsumer)
        gameEventService.emmit(gameEvent)

        then:
        1 * consumerA.accept(gameEvent)
        1 * consumerB.accept(gameEvent)
        0 * consumerB.accept(gameEvent)
    }

    def "should unregister inactive observer"() {
        given:
        Consumer<GameEvent> consumer = Mock(Consumer)
        Supplier<Boolean> isActiveFunction = Mock(Supplier)
        GameEvent gameEventA = Mock(GameEvent)
        GameEvent gameEventB = Mock(GameEvent)

        when:
        isActiveFunction.get() >>> [true, false]
        gameEventService.newObserver(isActiveFunction).onEvent(consumer)
        gameEventService.emmit(gameEventA)
        gameEventService.emmit(gameEventB)

        then:
        1 * consumer.accept(gameEventA)
        0 * consumer.accept(gameEventB)
    }

    def "should emmit game added event"() {
        given:
        Consumer<GameEvent> consumer = Mock(Consumer)

        when:
        gameEventService.newObserver({true}).onEvent(consumer)
        gameEventService.emmitGameAdded(new QueuedGame(id: 'abc123', channelId: 'ch01'))

        then:
        1 * consumer.accept(_) >> {
            assert it[0].gameType == 'ch01-name'
            assert it[0].gameId == 'abc123'
            assert it[0].eventType == 'GAME_ADDED'
        }
    }

    def "should emmit game started event"() {
        given:
        Consumer<GameEvent> consumer = Mock(Consumer)

        when:
        gameEventService.newObserver({true}).onEvent(consumer)
        gameEventService.emmitGameStarted(new QueuedGame(id: 'abc123', channelId: 'ch01'))

        then:
        1 * consumer.accept(_) >> {
            assert it[0].gameType == 'ch01-name'
            assert it[0].gameId == 'abc123'
            assert it[0].eventType == 'GAME_STARTED'
        }
    }

    def "should emmit game finished event"() {
        given:
        Consumer<GameEvent> consumer = Mock(Consumer)

        when:
        gameEventService.newObserver({true}).onEvent(consumer)
        gameEventService.emmitGameFinished(new QueuedGame(id: 'abc123', channelId: 'ch01'))

        then:
        1 * consumer.accept(_) >> {
            assert it[0].gameType == 'ch01-name'
            assert it[0].gameId == 'abc123'
            assert it[0].eventType == 'GAME_FINISHED'
        }
    }
}
