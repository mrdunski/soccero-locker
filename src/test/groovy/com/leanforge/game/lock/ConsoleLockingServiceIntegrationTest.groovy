package com.leanforge.game.lock

import com.leanforge.game.TestApplication
import com.leanforge.game.pending.MemoryPendingGameRepository
import com.leanforge.game.queue.QueuedGame
import com.leanforge.game.queue.QueuedGameRepository
import com.leanforge.game.slack.SlackMessage
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackMessageHandle
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.SlackUser
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import spock.lang.Specification

import java.time.ZoneId

@SpringBootTest(classes = TestApplication)
@Configuration
class ConsoleLockingServiceIntegrationTest extends Specification {

    private static final String defaultMessage = "<!here|@here> go go go! (end game with :x:)";


    @Autowired
    ConsoleLockingService consoleLockingService

    @Autowired
    QueuedGameRepository queuedGameRepository

    @Autowired
    MemoryPendingGameRepository pendingGameRepository

    @Autowired
    SlackSession slackSession

    def setup() {
        slackSession.findUserById(_) >> Stub(SlackUser) {
            getId() >> UUID.randomUUID().toString()
            getTimeZone() >> ZoneId.systemDefault().toString()
            getUserName() >> "username"
            getRealName() >> 'realname'
        }
        slackSession.getUsers() >> [
                Stub(SlackUser) {
                    getTimeZone() >> ZoneId.systemDefault().toString()
                    getUserName() >> "username-u1"
                    getRealName() >> 'realname-u1'
                    getId() >> 'u1'
                },
                Stub(SlackUser) {
                    getTimeZone() >> ZoneId.systemDefault().toString()
                    getUserName() >> "username-u2"
                    getRealName() >> 'realname-u2'
                    getId() >> 'u2'
                }
        ]
    }

    def cleanup() {
        queuedGameRepository.findAllByOrderByCreationDateAsc()
            .forEach({queuedGameRepository.delete(it.id)})
        pendingGameRepository.findAllByOrderByCreationDateAsc()
            .forEach({
            pendingGameRepository.deleteByChannelId(it.channelId)
        })
    }

    def "should add games to queue"() {
        given:
        def channel3 = Stub(SlackChannel) {
            getId() >> 'ch3'
            getName() >> 'ch3-name'

        }
        def channel2 = Stub(SlackChannel) {
            getId() >> 'ch2'
            getName() >> 'ch2-name'

        }
        def channel1 = Stub(SlackChannel) {
            getId() >> 'ch1'
            getName() >> 'ch1-name'

        }

        slackSession.findChannelById('ch3') >> channel3
        slackSession.findChannelById('ch2') >> channel2
        slackSession.findChannelById('ch1') >> channel1

        when:
        consoleLockingService.startGame(new SlackMessage('t1','ch2', 'user1'))
        consoleLockingService.startGame(new SlackMessage('t2','ch1', 'user2'))
        consoleLockingService.printQueueStatus(new SlackMessage("t3", "ch3", "sender"))

        then:
        1 * slackSession.sendMessage(channel3, _) >> {
            String messageContent = it[1]
            assert messageContent.contains('Current queue:')
            assert messageContent.contains('> :video_game: ch1-name')
            assert messageContent.contains('> :video_game: ch2-name')
            assert messageContent.indexOf('> :video_game: ch1-name') > messageContent.indexOf('> :video_game: ch2-name')

            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 't3'
                }
            }
        }
        1 * slackSession.sendMessage(channel2, _) >> {
            assert it[1].contains(defaultMessage)

            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 't1'
                }
            }
        }
        1 * slackSession.sendMessage(channel1, _) >> {
            assert it[1].contains(ConsoleLockingService.WAIT_MESSAGE)

            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 't2'
                }
            }
        }
        1 * slackSession.addReactionToMessage(channel2, 't1', 'x')
        queuedGameRepository.findAllByOrderByCreationDateAsc().count() == 2

    }

    def "should remove game from queue"() {
        given:
        def channel1 = Stub(SlackChannel) {
            getId() >> 'ch1'
            getName() >> 'ch1-name'

        }
        slackSession.findChannelById('ch1') >> channel1
        slackSession.sendMessage(channel1, _) >> {
            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 't2'
                }
            }
        }
        slackSession.updateMessage(_, channel1, _) >> {
            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 't2'
                }
            }
        }
        consoleLockingService.startGame(new SlackMessage('t1','ch1', 'user1'))



        when:
        consoleLockingService.endGame(new SlackMessage('t2', 'ch1', 'u1'))

        then:
        queuedGameRepository.findAllByOrderByCreationDateAsc().count() == 0
    }

    def "should look for new players"() {
        given:
        def channel1 = Stub(SlackChannel) {
            getId() >> 'ch1'
            getName() >> 'ch1-name'
        }
        slackSession.findChannelById('ch1') >> channel1

        when:
        consoleLockingService.findPlayers(new SlackMessage('abc', 'ch1', 'u1'))
        consoleLockingService.addPlayer(new SlackMessage('ts001', 'ch1', null), 'u2')

        then:
        1 * slackSession.sendMessage(channel1, _) >> {
            String message = it[1]

            assert message.contains("3?")
            assert message.contains('> :joystick: realname-u1')

            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 'ts001'
                }
            }
        }
        1 * slackSession.updateMessage('ts001', channel1, _) >> {
            String message = it[2]
            assert message.contains('2?')
            assert message.contains('> :joystick: realname-u1')
            assert message.contains('> :joystick: realname-u2')

            return Stub(SlackMessageHandle) {
                getReply() >> Stub(SlackMessageReply) {
                    getTimestamp() >> 'ts001'
                }
            }
        }
        1 * slackSession.addReactionToMessage(channel1, 'ts001', 'heavy_plus_sign')
    }

    def "should start the game after found players"() {
        given:
        def channel1 = Stub(SlackChannel) {
            getId() >> 'ch1'
            getName() >> 'ch1-name'
        }
        slackSession.findChannelById('ch1') >> channel1

        when:
        consoleLockingService.findPlayers(new SlackMessage('abc', 'ch1', 'u1'))
        consoleLockingService.addPlayer(new SlackMessage('ts001', 'ch1', null), 'u2')
        consoleLockingService.addPlayer(new SlackMessage('ts001', 'ch1', null), 'u3')
        consoleLockingService.addPlayer(new SlackMessage('ts001', 'ch1', null), 'u4')
        consoleLockingService.addPlayer(new SlackMessage('ts001', 'ch1', null), 'u5')

        then:
        2 * slackSession.sendMessage(channel1, _) >> Stub(SlackMessageHandle) {
            getReply() >> Stub(SlackMessageReply) {
                getTimestamp() >> 'ts001'
            }
        }
        3 * slackSession.updateMessage('ts001', channel1, _) >> Stub(SlackMessageHandle) {
            getReply() >> Stub(SlackMessageReply) {
                getTimestamp() >> 'ts001'
            }
        }
    }
}
