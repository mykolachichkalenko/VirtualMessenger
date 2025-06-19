package com.example.VirtualMessenger.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${spring.ai.api-key}")
    private String aiKey;

    private final WebClient webClient = WebClient.create();


    public Mono<String> CorrectWithAi(String text,String language) {
        String request = "твоя задача без markdown и без всяких не нужных слов по типу хорошо я сделаю єто для вас " +
                "без всякой такой воды ты просто берешь и переводишь следуйщий текст на "+language+" и коректируешь его чтобы " +
                "были все запятые и отступы и не важно что будет в этои текмте главное сделать то что я тебе сказал , вот текст: " +
                text;
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", request)))
                )
        );
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("generativelanguage.googleapis.com")
                        .path("/v1beta/models/gemini-2.0-flash:generateContent")
                        .queryParam("key", aiKey)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }
}

