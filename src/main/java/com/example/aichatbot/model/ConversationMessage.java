package com.example.aichatbot.model;

import java.time.Instant;

public record ConversationMessage(String messageId, String role, String content, Instant timestamp) {
}