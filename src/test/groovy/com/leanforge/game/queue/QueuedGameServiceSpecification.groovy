package com.leanforge.game.queue

import spock.lang.Specification
import spock.lang.Subject

import java.time.OffsetDateTime

class QueuedGameServiceSpecification extends Specification {

    def repository = Mock(QueuedGameRepository)

    @Subject
    QueuedGameService queuedGameService = new QueuedGameService(repository)

    def "should create new started game if queue is empty"() {
        given:
        repository.findAllByOrderByCreationDateAsc() >> [].stream()

        when:
        def game = queuedGameService.scheduleGame('ch1', 'u1')

        then:
        game.channelId == 'ch1'
        game.creatorId == 'u1'
        game.creationDate != null
        game.startDate != null
        1 * repository.save(_)
    }

    def "should create new scheduled game if queue is not empty"() {
        given:
        repository.findAllByOrderByCreationDateAsc() >> [new QueuedGame(startDate: OffsetDateTime.now())].stream()

        when:
        def game = queuedGameService.scheduleGame('ch1', 'u1')

        then:
        game.channelId == 'ch1'
        game.creatorId == 'u1'
        game.creationDate != null
        game.startDate == null
        1 * repository.save(_)
    }

    def "should end existing game"() {
        when:
        queuedGameService.endGame('abc123')

        then:
        1 * repository.delete('abc123')
    }

    def "should start game if no game in progress"() {
        given:
        def stoppedGame = new QueuedGame(id: 'abc123', startDate: null)
        repository.findAllByOrderByCreationDateAsc() >> [
                new QueuedGame(startDate: null),
                stoppedGame
        ].stream()
        repository.findOne('abc123') >> stoppedGame

        when:
        def startedGame = queuedGameService.startGame('abc123')

        then:
        startedGame.isPresent()
        1 * repository.save(_) >> {
            QueuedGame game = it[0]
            assert game.startDate != null
            assert game.id == 'abc123'
        }
    }

    def "should not start game if another game is in progress"() {
        given:
        def stoppedGame = new QueuedGame(id: 'abc123', startDate: null)
        repository.findAllByOrderByCreationDateAsc() >> [
                new QueuedGame(startDate: OffsetDateTime.now().minusMinutes(5)),
                stoppedGame
        ].stream()
        repository.findOne('abc123') >> stoppedGame

        when:
        def startedGame = queuedGameService.startGame('abc123')

        then:
        !startedGame.isPresent()
        0 * repository.save(_)
    }
}
