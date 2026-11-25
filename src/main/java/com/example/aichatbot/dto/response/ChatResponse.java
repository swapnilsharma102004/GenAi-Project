package com.example.aichatbot.dto.response;

import java.time.Instant;

public record ChatResponse(String conversationId, String messageId, String role, String content, Instant timestamp) {
}