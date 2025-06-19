package com.example.VirtualMessenger.Services.WebsocketService;

import com.example.VirtualMessenger.DTOs.MessageToReload;
import com.example.VirtualMessenger.DTOs.ReceiverPhone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReloadPageSignal implements WebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ReloadPageSignal(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String phone = getUserIdFromQuery(webSocketSession);
        userSessions.put(phone, webSocketSession);

        return webSocketSession.receive()
                .flatMap(msg -> {
                    ReceiverPhone receiverPhone;
                    try {
                         receiverPhone = getReceiverPhone(msg.getPayloadAsText());
                    } catch (JsonProcessingException e) {
                        return Flux.error(new RuntimeException(e));
                    }
                    WebSocketSession receiverSession = userSessions.get(receiverPhone.getReceiverPhone());

                    if (receiverSession != null && receiverSession.isOpen()) {
                        MessageToReload message = new MessageToReload();
                        message.setMessage("reload");
                        try {
                            return receiverSession.send(
                                    Mono.just(
                                            receiverSession.textMessage(serializeReceiverPhone(message))
                                    )
                            ).then();
                        } catch (JsonProcessingException e) {
                            return Flux.error(new RuntimeException(e));
                        }
                    }
                    return Mono.empty();
                })
                .doFinally(signal -> userSessions.remove(phone))
                .then();
    }

    private String getUserIdFromQuery(WebSocketSession session) {
        return session.getHandshakeInfo().getUri().getQuery().split("=")[1];
    }

    private ReceiverPhone getReceiverPhone(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ReceiverPhone.class);
    }
    private String serializeReceiverPhone (MessageToReload reload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(reload);
    }
}