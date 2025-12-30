package com.logistics.intellistock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration.class
})
public class IntellistockApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntellistockApplication.class, args);
    }
}