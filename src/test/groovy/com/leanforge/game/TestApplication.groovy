package com.leanforge.game

import com.leanforge.game.message.MemoryMessageBindingRepository
import com.leanforge.game.message.MessageBindingRepository
import com.leanforge.game.pending.MemoryPendingGameRepository
import com.leanforge.game.pending.PendingGameRepository
import com.leanforge.game.queue.QueuedGame
import com.leanforge.game.queue.QueuedGameRepository
import com.ullink.slack.simpleslackapi.SlackSession
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import spock.mock.DetachedMockFactory

import java.util.concurrent.CopyOnWriteArraySet
import java.util.stream.Stream

@SpringBootApplication
class TestApplication {

    private DetachedMockFactory factory = new DetachedMockFactory()

    @Bean
    SlackSession slackSession() {
        return factory.Mock(SlackSession)
    }

    @Bean
    @Primary
    QueuedGameRepository queuedGameRepository() {
        return new QueuedGameRepositoryImpl()
    }

    @Bean
    @Primary
    PendingGameRepository pendingGameRepository() {
        return new MemoryPendingGameRepository()
    }

    @Bean
    @Primary
    MessageBindingRepository messageBindingRepository() {
        return new MemoryMessageBindingRepository()
    }


    class QueuedGameRepositoryImpl implements QueuedGameRepository {

        private final Collection<QueuedGame> games = new CopyOnWriteArraySet<>();

        QueuedGame save(QueuedGame queuedGame) {
            games.add(queuedGame);
            return queuedGame
        }

        @Override
        Stream<QueuedGame> findAllByOrderByCreationDateAsc() {
            return games.sort({it.creationDate}).stream()
        }

        @Override
        int count() {
            return 0
        }

        void delete(String id) {
            games.parallelStream()
                    .filter({it.getId() == id})
                    .findAny()
                    .ifPresent({games.remove(it)});
        }

        @Override
        QueuedGame findOne(String id) {
            return games.parallelStream()
                    .filter({it.getId() == id})
                    .findAny().orElse(null)
        }
    }

}
