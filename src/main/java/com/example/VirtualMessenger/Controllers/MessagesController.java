package com.example.VirtualMessenger.Controllers;

import com.example.VirtualMessenger.DTOs.ChatPageShard;
import com.example.VirtualMessenger.DTOs.CorrectWithAi;
import com.example.VirtualMessenger.DTOs.Message;
import com.example.VirtualMessenger.DTOs.ReadMessages;
import com.example.VirtualMessenger.Services.AIService;
import com.example.VirtualMessenger.Services.CloudinaryService;
import com.example.VirtualMessenger.Services.MessageService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/messages")
public class MessagesController {

    private final MessageService messageService;
    private final CloudinaryService cloudinaryService;
    private final AIService aiService;

    public MessagesController(MessageService messageService, CloudinaryService cloudinaryService, AIService aiService) {
        this.messageService = messageService;
        this.cloudinaryService = cloudinaryService;
        this.aiService = aiService;
    }

    @GetMapping("/get")
    public Flux<Message> getMessagesByPage(ChatPageShard chatPageShard) {
        return messageService.getMessagesByPage(Long.parseLong(chatPageShard.getChatId()), chatPageShard.getShard(), Integer.parseInt(chatPageShard.getPage()));
    }

    @PostMapping("/mark-as-red")
    public Mono<Void> markAsRed(@RequestBody ReadMessages readMessages) {
        Long chatId = Long.parseLong(readMessages.getChatId());
        return messageService.markAsRed(readMessages.getShard(), chatId, readMessages.getPhoneUser());
    }

    @GetMapping(path = "/get/last/unred", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ServerSentEvent<Long>> getLastUnRedMessage(@RequestParam("chatId") Long chatId,
                                                           @RequestParam("shard") String shard,
                                                           @RequestParam("phoneUser") String userPhone) {
        return messageService.getLastUnMarked(chatId, shard, userPhone);
    }

    @PostMapping("/add/photo/{myPhone}/{receiverPhone}/{chatId}")
    public Mono<String> addImage(@PathVariable("myPhone") String myPhone,
                                 @PathVariable("receiverPhone") String receiverPhone,
                                 @PathVariable("chatId") String chatId,
                                 @RequestPart("photo") FilePart filePart) {
        return cloudinaryService.imageUpload(filePart,Long.parseLong(chatId),myPhone,receiverPhone);
    }
    @PostMapping("/add/video/{myPhone}/{receiverPhone}/{chatId}")
    public Mono<String> addVideo (@PathVariable("myPhone") String myPhone,
                                  @PathVariable("receiverPhone") String receiverPhone,
                                  @PathVariable("chatId") String chatId,
                                  @RequestPart("video") FilePart filePart){
        return cloudinaryService.videoUpload(filePart,Long.parseLong(chatId),myPhone,receiverPhone);
    }

    @PostMapping("/correct/ai")
    public Mono<String> correctWithAi(@RequestBody CorrectWithAi correctWithAi){
        return aiService.CorrectWithAi(correctWithAi.getText(),correctWithAi.getLanguage());
    }
}