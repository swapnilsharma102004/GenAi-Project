package com.example.aichatbot.controller;

import com.example.aichatbot.dto.request.ChatRequest;
import com.example.aichatbot.model.ConversationMessage;
import com.example.aichatbot.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> streamChat(@RequestBody ChatRequest request) {
        String message = request.message();
        String conversationId = request.conversationId();

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Flux.just("data: Message cannot be empty"));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(chatService.streamMessage(conversationId, message));
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<ConversationMessage>> getHistory(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatService.getHistory(conversationId));
    }

    @DeleteMapping("/clear/{conversationId}")
    public ResponseEntity<String> clearConversation(@PathVariable String conversationId) {
        chatService.clearConversation(conversationId);
        return ResponseEntity.ok("Conversation history cleared for ID: " + conversationId);
    }
}
