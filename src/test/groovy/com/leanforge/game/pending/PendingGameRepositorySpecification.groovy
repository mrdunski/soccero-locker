package com.leanforge.game.pending

import spock.lang.Specification
import spock.lang.Subject

import java.util.stream.Collectors


class PendingGameRepositorySpecification extends Specification {

    @Subject
    PendingGameRepository pendingGameRepository = new PendingGameRepository()

    def "should save and return game"() {
        given:
        def pendingGame = new PendingGame(
                playerCount: 4,
                playerIds: ['a', 'b', 'c'],
                channelId: 'ch123'
        )

        when:
        pendingGameRepository.save(pendingGame)
        def savedGame = pendingGameRepository.findByChannelId('ch123')

        then:
        savedGame.isPresent()
        savedGame.get().channelId == pendingGame.channelId
    }

    def "should replace old game"() {
        given:
        def oldPendingGame = new PendingGame(
                playerCount: 4,
                playerIds: ['a', 'b', 'c'],
                channelId: 'ch123'
        )
        def newPendingGame = new PendingGame(
                playerCount: 10,
                playerIds: ['w', 'x'],
                channelId: 'ch123'
        )
        pendingGameRepository.save(oldPendingGame)

        when:
        pendingGameRepository.save(newPendingGame)
        def savedGame = pendingGameRepository.findByChannelId('ch123')

        then:
        savedGame.isPresent()
        savedGame.get().channelId == newPendingGame.channelId
        savedGame.get().playerCount == 10
    }


    def "should delete existing game"() {
        given:
        def pendingGame = new PendingGame(
                playerCount: 4,
                playerIds: ['a', 'b', 'c'],
                channelId: 'ch123'
        )
        pendingGameRepository.save(pendingGame)

        when:
        pendingGameRepository.delete('ch123')
        def savedGame = pendingGameRepository.findByChannelId('ch123')

        then:
        !savedGame.isPresent()
    }

    def "should find all pending games"() {
        given:
        [
                new PendingGame(
                        playerCount: 4,
                        playerIds: ['a', 'b', 'c'],
                        channelId: 'ch1'
                ),
                new PendingGame(
                        playerCount: 4,
                        playerIds: ['a', 'b', 'c'],
                        channelId: 'ch2'
                ),
                new PendingGame(
                        playerCount: 4,
                        playerIds: ['a', 'b', 'c'],
                        channelId: 'ch3'
                )
        ].each {pendingGameRepository.save(it)}

        when:
        def all = pendingGameRepository.findAll().collect(Collectors.toList())

        then:
        all.any {it.channelId == 'ch1'}
        all.any {it.channelId == 'ch2'}
        all.any {it.channelId == 'ch3'}
    }
}
