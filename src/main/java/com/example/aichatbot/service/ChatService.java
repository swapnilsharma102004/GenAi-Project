
package com.example.aichatbot.service;

import com.example.aichatbot.cache.ChatCache;
import com.example.aichatbot.guard.CostGuard;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatService {

    private final CostGuard costGuard;
    private final ChatCache chatCache;
    private final String modelName;
    private final Client geminiClient;

    public ChatService(
            CostGuard costGuard,
            ChatCache chatCache,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model-name}") String modelName) {

        this.costGuard = costGuard;
        this.chatCache = chatCache;
        this.modelName = modelName;

        this.geminiClient = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String sendMessage(String message) {

        costGuard.validateWithError(message);

        String cached = chatCache.get(message);

        if (cached != null) {
            return cached;
        }

        // Call the actual Gemini API
        GenerateContentResponse response =
                geminiClient.models.generateContent(
                        modelName,
                        message,
                        null
                );

        String answer = response.text();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response."
            );
        }

        chatCache.put(message, answer);

        return answer;
    }

    public Flux<String> streamMessage(String message) {

        return Mono.fromCallable(() -> sendMessage(message))
                .subscribeOn(Schedulers.boundedElastic())
                .flux();
    }

    public void clearCache() {
        chatCache.clear();
    }
}
