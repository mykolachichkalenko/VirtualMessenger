package com.example.VirtualMessenger.Services.RedisService;

import com.example.VirtualMessenger.DTOs.User;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.BooleanSupplier;

@Service
public class UserRedis {
    private final ReactiveRedisTemplate<String, User> reactiveRedisTemplate;
    private final Duration ttl = Duration.ofMinutes(30);

    public UserRedis(ReactiveRedisTemplate<String, User> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<User> getUserByPhone(String phone) {
        return reactiveRedisTemplate.opsForValue().get("user::phone::" + phone);
    }

    public Mono<Boolean> cacheUser(User user){
        Mono<Boolean> byId = reactiveRedisTemplate.opsForValue()
                .set("user::id::" + user.getId(),user,ttl);

        Mono<Boolean> byPhone = reactiveRedisTemplate.opsForValue()
                .set("user::phone::" + user.getPhoneNumber(),user,ttl);

        return Mono.zip(byId,byPhone).map(tuple -> tuple.getT1() && tuple.getT2());
    }

    public Mono<Boolean> evictUser(String phone) {
        return reactiveRedisTemplate.delete("user::phone::"+phone)
                .thenReturn(true);
    }
}