package com.example.VirtualMessenger.Configs;

import com.example.VirtualMessenger.DTOs.Message;
import com.example.VirtualMessenger.DTOs.User;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, User> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<User> valueSerializer = new Jackson2JsonRedisSerializer<>(User.class);
        valueSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisSerializationContext<String, User> context = RedisSerializationContext
                .<String, User>newSerializationContext(keySerializer)
                .value(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<Message>> reactiveMessageListRedisTemplate(ReactiveRedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class);

        Jackson2JsonRedisSerializer<List<Message>> valueSerializer = new Jackson2JsonRedisSerializer<>(listType);
        valueSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisSerializationContext<String, List<Message>> context = RedisSerializationContext
                .<String, List<Message>>newSerializationContext(keySerializer)
                .value(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}