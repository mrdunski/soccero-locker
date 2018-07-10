package com.leanforge.game.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leanforge.game.event.model.Command;
import com.leanforge.game.queue.QueuedGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Controller
public class GameEventSocketHandler extends TextWebSocketHandler {

    @Autowired
    GameEventService gameEventService;

    @Autowired
    QueuedGameService queuedGameService;

    @Autowired
    ObjectMapper objectMapper;

    private Collection<WebSocketSession> sessions = Collections.synchronizedCollection(new ArrayList<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        gameEventService.newObserver(session::isOpen)
                .onEvent(gameEvent -> {
                    try {
                        session.sendMessage(new TextMessage(toJson(gameEvent)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Command command = objectMapper.readValue(message.getPayload(), Command.class);

        if (command == null || !"GET_STATE".equals(command.getCommandName())) {
            return;
        }

        queuedGameService.scheduledGames()
                .map(gameEventService::createGameAddedEvent)
                .map(this::toJson)
                .map(TextMessage::new)
                .forEach(it -> {
                    try {
                        session.sendMessage(it);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });

        queuedGameService.findStartedGame().map(gameEventService::createGameStartedEvent)
                .map(this::toJson)
                .map(TextMessage::new)
                .ifPresent(it -> {
                    try {
                        session.sendMessage(it);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    private String toJson(Object pojo) {
        try {
            return objectMapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void sendPingMessage() {
        sessions.removeIf(it -> !it.isOpen());
        sessions.forEach(it -> {
            try {
                it.sendMessage(new PingMessage());
            } catch (IOException e) {
            }
        });
    }
}
