package com.example.aichatbot.service;

import com.example.aichatbot.cache.ChatCache;
import com.example.aichatbot.cache.ConversationCache;
import com.example.aichatbot.guard.CostGuard;
import com.example.aichatbot.model.Conversation;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final CostGuard costGuard;
    private final ChatCache chatCache;
    private final ConversationCache conversationCache;
    private final String modelName;
    private final Client geminiClient;

    public ChatService(
            CostGuard costGuard,
            ChatCache chatCache,
            ConversationCache conversationCache,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model-name}") String modelName) {

        this.costGuard = costGuard;
        this.chatCache = chatCache;
        this.conversationCache = conversationCache;
        this.modelName = modelName;

        this.geminiClient = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String sendMessage(String message) {
        return sendMessage(null, message);
    }

    public String sendMessage(String conversationId, String message) {
        costGuard.validateWithError(message);

        String effectiveId = (conversationId == null || conversationId.isBlank()) ? "default" : conversationId;
        Conversation conversation = conversationCache.getOrCreate(effectiveId);

        // Add user message to history
        conversation.addMessage("user", message);

        // Convert history to Gemini parts
        List<Content> contents = conversation.getMessages().stream()
                .map(msg -> Content.builder()
                        .role(msg.role().equals("user") ? "user" : "model")
                        .parts(List.of(Part.builder().text(msg.content()).build()))
                        .build())
                .collect(Collectors.toList());

        // Call Gemini API with history
        GenerateContentResponse response = geminiClient.models.generateContent(
                modelName,
                contents,
                null
        );

        String answer = response.text();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty response.");
        }

        // Add assistant response to history
        conversation.addMessage("model", answer);

        return answer;
    }

    public Flux<String> streamMessage(String message) {
        return streamMessage(null, message);
    }

    public Flux<String> streamMessage(String conversationId, String message) {
        costGuard.validateWithError(message);

        String effectiveId = (conversationId == null || conversationId.isBlank()) ? "default" : conversationId;
        Conversation conversation = conversationCache.getOrCreate(effectiveId);
        conversation.addMessage("user", message);

        List<Content> contents = conversation.getMessages().stream()
                .map(msg -> Content.builder()
                        .role(msg.role().equals("user") ? "user" : "model")
                        .parts(List.of(Part.builder().text(msg.content()).build()))
                        .build())
                .collect(Collectors.toList());

        return Flux.create(sink -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = geminiClient.models.generateContentStream(
                        modelName,
                        contents,
                        null
                );

                StringBuilder fullResponse = new StringBuilder();

                for (GenerateContentResponse response : responseStream) {
                    String text = response.text();
                    if (text != null) {
                        fullResponse.append(text);
                        sink.next(text);
                    }
                }

                // Save assistant message to history
                conversation.addMessage("model", fullResponse.toString());
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).cast(String.class);
    }

    public void clearCache() {
        chatCache.clear();
    }
}