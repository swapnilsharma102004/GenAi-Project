package com.example.aichatbot.cache;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ChatCache {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TIMEOUT_MS = 5 * 60 * 1000;

    public String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() - entry.timestamp > CACHE_TIMEOUT_MS) {
            cache.remove(key);
            return null;
        }
        return entry.response;
    }

    public void put(String key, String response) {
        cache.put(key, new CacheEntry(response));
    }

    public void clear() {
        cache.clear();
    }

    private static class CacheEntry {
        final String response;
        final long timestamp;

        CacheEntry(String response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
