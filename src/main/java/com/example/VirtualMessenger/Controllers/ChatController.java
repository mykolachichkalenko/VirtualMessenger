package com.example.VirtualMessenger.Controllers;

import com.example.VirtualMessenger.DTOs.Chat;
import com.example.VirtualMessenger.DTOs.UnRead;
import com.example.VirtualMessenger.Services.ChatService;
import com.example.VirtualMessenger.Services.MessageService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    public ChatController(ChatService chatService, MessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @GetMapping("/with/{phoneInterlocutor}")
    public Mono<Chat> getChatByPhones(@PathVariable String phoneInterlocutor, Authentication authentication) {
        return chatService.getChatByUsersPhones(phoneInterlocutor, authentication);
    }
    @GetMapping(path = "/get/my/all",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Chat>> getAllChatByPhone(Authentication authentication){
        return chatService.getAllChatsByPhone(authentication);
    }
    @PostMapping("/set/unread")
    public Mono<Void> setUnRead(@RequestBody UnRead unRead){
        return messageService.setUnReadForFirstUser(unRead.getReceiverPhone(),unRead.getSenderPhone());
    }
    @PostMapping("/set/red/me")
    public Mono<Void> setRed(@RequestBody UnRead unRead){
        return chatService.setRedForMe(unRead.getReceiverPhone(), unRead.getSenderPhone());
    }
}
