package com.example.VirtualMessenger.Services.WebsocketService;

import com.example.VirtualMessenger.DTOs.Message;
import com.example.VirtualMessenger.Services.ChatService;
import com.example.VirtualMessenger.Services.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageHandler implements WebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ChatService chatService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public MessageHandler(ChatService chatService, MessageService messageService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String phone = getUserIdFromQuery(webSocketSession);
        userSessions.put(phone, webSocketSession);

        return webSocketSession.receive()
                .flatMap(message -> {
                    Message userMessage = new Message();
                    try {
                        userMessage = parseMessage(message.getPayloadAsText());

                        Message finalUserMessage = userMessage;
                        return chatService.getIdChatOrGetIdChatAndCreateIdChatWithPhones(userMessage.getReceiverPhone(), userMessage.getSenderPhone())
                                .flatMap(chatId -> {
                                    Message msg = new Message();
                                    msg.setId(0L);
                                    msg.setChatId(chatId);
                                    msg.setRead(false);
                                    msg.setContent(finalUserMessage.getContent());
                                    msg.setType(finalUserMessage.getType());
                                    msg.setSentAt(LocalDateTime.now());
                                    msg.setReceiverPhone(finalUserMessage.getReceiverPhone());
                                    msg.setSenderPhone(finalUserMessage.getSenderPhone());

                                    WebSocketSession recipientSession = userSessions.get(finalUserMessage.getReceiverPhone());

                                    if (recipientSession != null && recipientSession.isOpen()) {
                                        return recipientSession.send(Mono.just(
                                                recipientSession.textMessage(serializeMessage(msg))
                                        )).then(messageService.addMessage(msg));
                                    }
                                    return messageService.addMessageAndSetUnread(msg).then();
                                });
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
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
