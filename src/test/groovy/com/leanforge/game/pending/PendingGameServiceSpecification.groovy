package com.leanforge.game.pending

import spock.lang.Specification
import spock.lang.Subject

import java.time.OffsetDateTime

class PendingGameServiceSpecification extends Specification {

    PendingGameRepository repository = Mock(PendingGameRepository)

    @Subject
    PendingGameService pendingGameService = new PendingGameService(repository)

    def "should create and save new pending game"() {
        given:
        String channelId = 'abc123'
        String creatorId = 'creatora'
        OffsetDateTime testStart = OffsetDateTime.now()

        when:
        def game = pendingGameService.addPendingGame(channelId, creatorId, 4, PendingGame.GameType.CONSOLE)

        then:
        game.channelId == channelId
        game.creatorId == creatorId
        testStart.isBefore(game.creationDate) || testStart.isEqual(game.creationDate)
        OffsetDateTime.now().isEqual(game.creationDate) || OffsetDateTime.now().isAfter(game.creationDate)
        game.playerCount == 4
        game.playerIds.contains(creatorId)
        game.playerIds.size() == 1
        1 * repository.deleteByChannelId(channelId)
        1 * repository.save(_)
    }

    def "should throw exception if no player provided"() {
        when:
        pendingGameService.addPlayers(new PendingGame(channelId: 'ch1'))

        then:
        thrown(IllegalArgumentException)
    }

    def "should not create duplicated players"() {
        given:
        def game = new PendingGame(channelId: 'ch1', playerIds: ['a', 'b', 'c'], playerCount: 4)

        when:
        pendingGameService.addPlayers(game, 'a', 'p1', 'p1', 'b', 'b')

        then:
        1 * repository.save(_) >> {
            assert it[0].channelId == 'ch1'
            assert it[0].playerIds == ['a', 'b', 'c', 'p1']
        }
    }

    def "should limit players in pending game"() {
        given:
        def game = new PendingGame(channelId: 'ch1', playerIds: ['a', 'b', 'c'], playerCount: 4)

        when:
        pendingGameService.addPlayers(game, 'd', 'e')

        then:
        1 * repository.save(_) >> {
            assert it[0].channelId == 'ch1'
            assert it[0].playerIds == ['a', 'b', 'c', 'd']
        }
    }
}
