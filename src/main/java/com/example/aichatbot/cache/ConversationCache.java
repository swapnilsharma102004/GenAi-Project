package com.example.aichatbot.cache;

import com.example.aichatbot.model.Conversation;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationCache {
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    public Conversation get(String conversationId) {
        return conversations.get(conversationId);
    }

    public void put(String conversationId, Conversation conversation) {
        conversations.put(conversationId, conversation);
    }

    public Conversation getOrCreate(String conversationId) {
        if (conversationId == null) {
            Conversation newConv = new Conversation(null);
            conversations.put(newConv.getConversationId(), newConv);
            return newConv;
        }
        return conversations.computeIfAbsent(conversationId, Conversation::new);
    }
}