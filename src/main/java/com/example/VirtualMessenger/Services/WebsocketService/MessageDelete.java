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
public class MessageDelete implements WebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ChatService chatService;

    public MessageDelete(ObjectMapper objectMapper, MessageService messageService, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.chatService = chatService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String phone = getUserIdFromQuery(webSocketSession);
        userSessions.put(phone, webSocketSession);

        return webSocketSession.receive()
                .flatMap(msg -> {
                    Message message = new Message();
                    try {
                        message = parseMessage(msg.getPayloadAsText());

                        Message finalMessage = message;
                        if (message.getChatId() == null) {
                            return chatService.getIdChatOrGetIdChatAndCreateIdChatWithPhones(message.getReceiverPhone(), message.getSenderPhone()) //deleting if chat id is empty
                                    .flatMap(chatId -> {
                                        finalMessage.setChatId(chatId);
                                        return messageService.getShardByChatId(chatId)
                                                .flatMap(id -> {
                                                            WebSocketSession recipientSession = userSessions.get(finalMessage.getReceiverPhone());

                                                            if (recipientSession != null && recipientSession.isOpen()) {
                                                                return recipientSession.send(
                                                                        Mono.just(
                                                                                recipientSession.textMessage(serializeMessage(finalMessage))
                                                                        )
                                                                ).then(messageService.deleteMessageSafely(finalMessage, String.valueOf(id)));
                                                            }
                                                            return messageService.deleteMessageSafely(finalMessage, String.valueOf(id));
                                                        }
                                                );
                                    });
                        }
                        return messageService.getShardByChatId(message.getChatId()) //deleting if chat id is not empty
                                .flatMap(id -> {
                                    WebSocketSession recipientSession = userSessions.get(finalMessage.getReceiverPhone());

                                    if (recipientSession != null && recipientSession.isOpen()) {
                                        return recipientSession.send(
                                                Mono.just(
                                                        recipientSession.textMessage(serializeMessage(finalMessage))
                                                )
                                        ).then(messageService.deleteMessageSafely(finalMessage, String.valueOf(id)));
                                    }
                                    return messageService.deleteMessageSafely(finalMessage, String.valueOf(id));
                                });
                    } catch (JsonProcessingException e) {
                        return Flux.error(new RuntimeException(e));
                    }
                })
                .doFinally(signal -> userSessions.remove(phone))
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
