package com.example.VirtualMessenger.Services.WebsocketService;

import com.example.VirtualMessenger.DTOs.Message;
import com.example.VirtualMessenger.Services.ChatService;
import com.example.VirtualMessenger.Services.MessageService;
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
public class MessageEdit implements WebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final MessageService messageService;

    public MessageEdit(ObjectMapper objectMapper, ChatService chatService, MessageService messageService) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String phone = getUserIdFromQuery(webSocketSession);
        userSessions.put(phone, webSocketSession);

        return webSocketSession.receive()
                .flatMap(msg -> {
                    try {
                        Message message = parseMessage(msg.getPayloadAsText());

                        if (message.getChatId() == null){           //updating if chatId is empty
                            return chatService.getIdChatOrGetIdChatAndCreateIdChatWithPhones(message.getReceiverPhone(),message.getSenderPhone())
                                    .flatMap(chatId -> {
                                        message.setChatId(chatId);
                                        return messageService.getShardByChatId(chatId)
                                                .flatMap(shard -> {
                                                    WebSocketSession session = userSessions.get(message.getReceiverPhone());

                                                    if (session != null && session.isOpen()){
                                                        return session.send(
                                                                Mono.just(
                                                                        session.textMessage(serializeMessage(message))
                                                                )
                                                        ).then(messageService.updateMessage(message,String.valueOf(shard)));
                                                    }
                                                    return messageService.updateMessage(message,String.valueOf(shard));
                                                });
                                    });
                        }
                        return messageService.getShardByChatId(message.getChatId())
                                .flatMap(shard -> {
                                    WebSocketSession session = userSessions.get(message.getReceiverPhone());

                                    if (session != null && session.isOpen()){
                                        return session.send(
                                                Mono.just(
                                                        session.textMessage(serializeMessage(message))
                                                )
                                        ).then(messageService.updateMessage(message,String.valueOf(shard)));
                                    }
                                    return messageService.updateMessage(message,String.valueOf(shard));
                                });
                    } catch (JsonProcessingException e) {
                        return Flux.error(new RuntimeException(e));
                    }
                })
                .doFinally(signalType -> userSessions.remove(phone))
                .then();
    }

    private String getUserIdFromQuery(WebSocketSession session) {
        return session.getHandshakeInfo().getUri().getQuery().split("=")[1];
    }

    private Message parseMessage(String json) throws JsonProcessingException {
        try {
            return objectMapper.readValue(json, Message.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeMessage(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
