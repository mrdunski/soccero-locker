package com.leanforge.game

import com.ullink.slack.simpleslackapi.SlackSession
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import spock.mock.DetachedMockFactory

@SpringBootApplication
class TestApplication {

    private DetachedMockFactory factory = new DetachedMockFactory()

    @Bean
    SlackSession slackSession() {
        return factory.Mock(SlackSession)
    }
}
