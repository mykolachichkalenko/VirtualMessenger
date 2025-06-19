package com.example.VirtualMessenger.Services.RedisService;

import com.example.VirtualMessenger.DTOs.Message;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class MessagesRedis {
    private final ReactiveRedisTemplate<String, List<Message>> reactiveRedisTemplate;
    private final ReactiveRedisTemplate<String,String> reactiveStringRedisTemplate;
    private final Duration ttl = Duration.ofMinutes(10);

    public MessagesRedis(ReactiveRedisTemplate<String, List<Message>> reactiveRedisTemplate, ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    private String buildKey(long chatId, int page) {
        return "messages::chat::" + chatId + "::page::" + page;
    }

    private String getChatPageIndexKey(long chatId) {
        return "messages::chat::" + chatId + "::pages";
    }

    public Mono<List<Message>> getMessagesByChatAndPage(long chatId, int page) {
        return reactiveRedisTemplate.opsForValue().get(buildKey(chatId, page));
    }

    public Mono<Boolean> cacheMessagesByChatAndPage(long chatId, int page, List<Message> messages) {
        String key = buildKey(chatId, page);
        String indexKey = getChatPageIndexKey(chatId);
        System.out.println("caching");

        return reactiveRedisTemplate.opsForValue()
                .set(key, messages, ttl)
                .then(reactiveStringRedisTemplate.opsForSet()
                        .add(indexKey, key))
                .thenReturn(true);
    }

    public Mono<Boolean> evictMessagesByChat(long chatId) {
        String indexKey = getChatPageIndexKey(chatId);
        System.out.println("delete cache");

        return reactiveStringRedisTemplate.opsForSet()
                .members(indexKey)
                .collectList()
                .flatMap(keys -> reactiveStringRedisTemplate.delete(Flux.fromIterable(keys)))
                .then(reactiveStringRedisTemplate.delete(indexKey))
                .thenReturn(true);
    }
}

