package com.example.aichatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    private String apiKey;
    private String modelName;

    public GeminiConfig(@Value("${gemini.api-key}") String apiKey,
                       @Value("${gemini.model-name}") String modelName) {
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    @Bean
    public String geminiApiKey() {
        if (apiKey == null) {
            throw new IllegalStateException(
                "GEMINI_API_KEY is required. Set it as an environment variable: export GEMINI_API_KEY=your-key");
        }
        return apiKey;
    }

    @Bean
    public String geminiModelName() {
        return modelName;
    }
}
