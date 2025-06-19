package com.example.VirtualMessenger.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.VirtualMessenger.DTOs.Message;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final MessageService messageService;

    public CloudinaryService(Cloudinary cloudinary, MessageService messageService) {
        this.cloudinary = cloudinary;
        this.messageService = messageService;
    }

    public Mono<String> imageUpload(FilePart filePart, Long chatId, String myPhone, String receiverPhone) {

        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return Mono.fromCallable(() -> {
                                Map uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.emptyMap());
                                String url = uploadResult.get("secure_url").toString();

                                Message message = new Message();
                                message.setChatId(chatId);
                                message.setContent(url);
                                message.setReceiverPhone(receiverPhone);
                                message.setSenderPhone(myPhone);
                                message.setType("PHOTO");

                                return message;
                            }).subscribeOn(Schedulers.boundedElastic())
                            .flatMap(messageService::addMessageAndSetUnread)
                            .thenReturn("added");
                });
    }

    public Mono<String> videoUpload(FilePart video,Long chatId,String myPhone,String receiverPhone){

        return DataBufferUtils.join(video.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return Mono.fromCallable(() -> {
                        Map uploadResult = cloudinary.uploader().uploadLarge(bytes,ObjectUtils.asMap(
                                "resource_type","video",
                                "format","mp4"));

                        String url = Objects.toString(uploadResult.get("url"),"");

                        Message message = new Message();
                        message.setContent(url);
                        message.setChatId(chatId);
                        message.setType("VIDOE");
                        message.setSenderPhone(myPhone);
                        message.setReceiverPhone(receiverPhone);

                        return message;
                    }).subscribeOn(Schedulers.boundedElastic())
                            .flatMap(messageService::addMessageAndSetUnread)
                            .thenReturn("reload");
                });
    }
}