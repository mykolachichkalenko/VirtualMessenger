package com.example.VirtualMessenger.Configs;

import com.example.VirtualMessenger.Services.WebsocketService.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFlux
public class WebSocketConfig {

    private final IsTypingUserHandler isTypingUserHandler;
    private final MessageHandler messageHandler;
    private final MessageDelete messageDelete;
    private final MessageEdit messageEdit;
    private final ReloadPageSignal reloadPageSignal;
    private final DeletingChat deletingChat;

    public WebSocketConfig(IsTypingUserHandler isTypingUserHandler, MessageHandler messageHandler, MessageDelete messageDelete, MessageEdit messageEdit, ReloadPageSignal reloadPageSignal, DeletingChat deletingChat) {
        this.isTypingUserHandler = isTypingUserHandler;
        this.messageHandler = messageHandler;
        this.messageDelete = messageDelete;
        this.messageEdit = messageEdit;
        this.reloadPageSignal = reloadPageSignal;
        this.deletingChat = deletingChat;
    }


    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/chat/isTyping", isTypingUserHandler);
        map.put("/ws/chat/message", messageHandler);
        map.put("/ws/chat/message/delete",messageDelete);
        map.put("/ws/chat/message/edit",messageEdit);
        map.put("/ws/chat/reload",reloadPageSignal);
        map.put("/ws/chat/delete",deletingChat);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(-1);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}