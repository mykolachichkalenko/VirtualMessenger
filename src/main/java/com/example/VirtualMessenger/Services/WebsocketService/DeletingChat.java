package com.example.VirtualMessenger.Services.WebsocketService;

import com.example.VirtualMessenger.DTOs.TypingMessage;
import com.example.VirtualMessenger.Services.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeletingChat implements WebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ChatService chatService;

    public DeletingChat(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String phone = getUserIdFromQuery(webSocketSession);
        userSessions.put(phone, webSocketSession);

        return webSocketSession.receive()
                .flatMap(id -> {
                    try {
                        Long idLong = parseId(id.getPayloadAsText());
                        return chatService.getInterlocutorPhone(phone, idLong)
                                .flatMap(interlocutorPhone -> {
                                    WebSocketSession interlocutorSession = userSessions.get(interlocutorPhone);

                                    if (interlocutorSession != null && interlocutorSession.isOpen()) {
                                        return chatService.deleteChatById(idLong)
                                                .then(interlocutorSession.send(Mono.just(
                                                interlocutorSession.textMessage(serialize(idLong)))
                                        ));
                                    }
                                    return chatService.deleteChatById(idLong);
                                });
                    } catch (JsonProcessingException e) {
                        return reactor.core.publisher.Flux.error(new RuntimeException(e));
                    }
                })
                .doFinally(signal -> userSessions.remove(phone))
                .then();
    }

    private String getUserIdFromQuery(WebSocketSession session) {
        return session.getHandshakeInfo().getUri().getQuery().split("=")[1];
    }

    private Long parseId(String json) throws JsonProcessingException {
        try {
            return new ObjectMapper().readValue(json, Long.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(Long id) {
        try {
            return new ObjectMapper().writeValueAsString(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
