package com.example.aichatbot.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    private final String conversationId;
    private final Instant createdAt;
    private final List<ConversationMessage> messages;

    public Conversation(String conversationId) {
        this.conversationId = conversationId != null ? conversationId : UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.messages = new ArrayList<>();
    }

    public String getConversationId() { return conversationId; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ConversationMessage> getMessages() { return messages; }

    public void addMessage(String role, String content) {
        String messageId = UUID.randomUUID().toString();
        messages.add(new ConversationMessage(messageId, role, content, Instant.now()));
    }
}