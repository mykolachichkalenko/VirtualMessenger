package com.example.VirtualMessenger.Services.WebsocketService;

import com.example.VirtualMessenger.DTOs.TypingMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IsTypingUserHandler implements WebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String phone = getUserIdFromQuery(session);
        userSessions.put(phone, session);

        return session.receive()
                .flatMap(message -> {
                    TypingMessage typing;
                    try {
                        typing = parseMessage(message.getPayloadAsText());

                        WebSocketSession recipientSession = userSessions.get(typing.getToUserPhone());

                        if (recipientSession != null && recipientSession.isOpen()) {
                            return recipientSession.send(Mono.just(
                                    recipientSession.textMessage(
                                            serialize(typing)
                                    )
                            ));
                        }
                        return Mono.empty();
                    } catch (JsonProcessingException e) {
                        return Mono.empty();
                    }
                })
                .doFinally(signal -> userSessions.remove(phone))
                .then();
    }

    private String getUserIdFromQuery(WebSocketSession session) {
        return session.getHandshakeInfo().getUri().getQuery().split("=")[1];
    }

    private TypingMessage parseMessage(String json) throws JsonProcessingException {
        try {
            return new ObjectMapper().readValue(json, TypingMessage.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(TypingMessage typing) {
        try {
            return new ObjectMapper().writeValueAsString(typing);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}