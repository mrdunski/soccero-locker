package com.leanforge.game.queue

import spock.lang.Specification
import spock.lang.Subject

import java.util.stream.Collectors

class QueuedGameRepositorySpecification extends Specification {


    @Subject
    QueuedGameRepository queuedGameRepository = new QueuedGameRepository()

    def "should save the games and list it"() {
        given:
        def game = new QueuedGame(creatorId: 'psz')

        when:
        queuedGameRepository.save(game)
        def all = queuedGameRepository.findAllOrderedByCreationDateAsc()

        then:
        all.collect(Collectors.toList()).contains(game)
    }

    def "should delete the game"() {
        given:
        def game = new QueuedGame(creatorId: 'psz')
        queuedGameRepository.save(game)

        when:
        queuedGameRepository.delete(game.id)

        then:
        !queuedGameRepository.findAllOrderedByCreationDateAsc().collect(Collectors.toList()).contains(game)
    }
}
