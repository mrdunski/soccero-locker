package com.leanforge.game.lock

import com.leanforge.game.message.MessageBindingService
import com.leanforge.game.pending.PendingGameMessages
import com.leanforge.game.pending.PendingGameService
import com.leanforge.game.queue.QueuedGame
import com.leanforge.game.queue.QueuedGameMessages
import com.leanforge.game.queue.QueuedGameService
import com.leanforge.game.slack.SlackMessage
import com.leanforge.game.slack.SlackService
import spock.lang.Specification
import spock.lang.Subject

import java.time.OffsetDateTime

class ConsoleLockingServiceSpecification extends Specification {

    QueuedGameService queuedGameService = Mock(QueuedGameService)
    PendingGameService pendingGameService = Mock(PendingGameService)
    SlackService slackService = Mock(SlackService)
    QueuedGameMessages queuedGameMessages = Mock(QueuedGameMessages)
    MessageBindingService messageBindingService = Mock(MessageBindingService)
    PendingGameMessages pendingGameMessages = Mock(PendingGameMessages)

    @Subject
    ConsoleLockingService lockingService = new ConsoleLockingService(queuedGameService, queuedGameMessages, pendingGameService, pendingGameMessages, messageBindingService, slackService)

    def setup() {
        pendingGameService.allPendingGames() >> [].stream()
    }

    def "should schedule new game and send notification if already started"() {
        given:
        def message = new SlackMessage('abc', 'ch1', 'us21')
        queuedGameService.scheduledGames() >> [].stream()

        when:
        lockingService.startGame(message)

        then:
        1 * queuedGameService.scheduleGame('ch1', 'us21') >> new QueuedGame(channelId: 'ch1', creatorId: 'us21', startDate: OffsetDateTime.now(), id: 'abc123')
        1 * slackService.sendChannelMessage('ch1', ConsoleLockingService.GO_MESSAGE, 'x') >> new SlackMessage('bcd', 'ch1', 'test')
        1 * messageBindingService.bind(_, 'abc123') >> {
            assert it[0].timestamp == 'bcd'
        }
    }

    def "should schedule new game and ask to wait if game wasn't started"() {
        given:
        def message = new SlackMessage('abc', 'ch1', 'us21')
        queuedGameMessages.statusMessage(_) >> ''
        queuedGameService.scheduledGames() >> [].stream()

        when:
        lockingService.startGame(message)

        then:
        1 * queuedGameService.scheduleGame('ch1', 'us21') >> new QueuedGame(channelId: 'ch1', creatorId: 'us21', startDate: null, id: 'abc123')
        1 * slackService.sendChannelMessage('ch1', ConsoleLockingService.WAIT_MESSAGE + '> ') >> new SlackMessage('bcd', 'ch1', 'test')
    }

    def "should end game"() {
        given:
        def message = new SlackMessage('abc', 'ch1', 'us21')
        messageBindingService.findBindingId(message) >> Optional.of('abc123')
        queuedGameService.startOldestGame() >> Optional.empty()

        when:
        lockingService.endGame(message)

        then:
        1 * queuedGameService.endGame('abc123')
        1 * slackService.addReactions(message, 'ok_hand')
    }

    def "should move queue up"() {
        given:
        def message = new SlackMessage('abc', 'ch1', 'us21')
        def startedGame = new QueuedGame(channelId: 'ch01')
        def waitingGame = new QueuedGame(channelId: 'ch02')
        messageBindingService.findBindingId(message) >> Optional.of(startedGame.id)

        when:
        lockingService.endGame(message)

        then:
        1 * queuedGameService.startOldestGame() >> {
            waitingGame.startDate = OffsetDateTime.now()
            Optional.of(waitingGame)
        }
        1 * slackService.sendChannelMessage('ch02', ConsoleLockingService.GO_MESSAGE, 'x')

    }

    def "should remove expired game"() {
        given:
        def expiredGame = new QueuedGame(startDate: OffsetDateTime.now().minusMinutes(60), channelId: 'ch002')
        queuedGameService.findStartedGame() >> Optional.of(expiredGame)

        when:
        lockingService.removeOldGames()

        then:
        1 * queuedGameService.endGame(expiredGame.id)
        1 * queuedGameService.startOldestGame() >> Optional.empty()
        1 * slackService.sendChannelMessage('ch002', _)
    }

    def "should not remove non-expired game"() {
        given:
        def nonExpiredGame = new QueuedGame(startDate: OffsetDateTime.now().minusMinutes(15))
        queuedGameService.findStartedGame() >> Optional.of(nonExpiredGame)

        when:
        lockingService.removeOldGames()

        then:
        0 * queuedGameService.endGame(_)
    }

    def "should print queue status"() {
        given:
        queuedGameService.scheduledGames() >> [new QueuedGame(channelId: 'a1'), new QueuedGame(channelId: 'a2')].stream()
        queuedGameMessages.statusMessage(_) >> { " -${it[0].channelId}- "}

        when:
        lockingService.printQueueStatus(new SlackMessage('abc', 'a2', 'us1'))

        then:
        1 * slackService.sendChannelMessage('a2', _) >> {
            String message = it[1]

            assert message.contains('-a1-')
            assert message.contains('-a2-')
        }
    }
}
