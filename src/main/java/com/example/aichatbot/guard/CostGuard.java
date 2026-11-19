package com.example.aichatbot.guard;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CostGuard {

    private static final int MAX_INPUT_LENGTH = 10000;

    public void validateWithError(String message) {
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (message.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                "Message exceeds maximum length of " + MAX_INPUT_LENGTH + " characters");
        }
    }
}