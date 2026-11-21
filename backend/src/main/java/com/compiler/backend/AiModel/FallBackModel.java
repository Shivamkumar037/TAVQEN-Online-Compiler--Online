
package com.compiler.backend.AiModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FallBackModel {

    private final AiModels aiModels;
    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.create();

    @Value("${ai.chat.max-output-tokens:16384}")
    private int maxOutputTokens;

    @Value("${ai.chat.temperature:0.3}")
    private double temperature;

    public String generateResponse(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        // 1. Gemini
        try {
            if (aiModels.getGemini().isEnabled()
                    && hasKey(aiModels.getGemini().getApiKey())) {

                String response = callGemini(prompt);

                if (isValid(response)) {
                    log.info("AI response received from Gemini");
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("Gemini failed: {}", e.getMessage());
        }

        // 2. Groq
        try {
            if (aiModels.getGroq().isEnabled()
                    && hasKey(aiModels.getGroq().getApiKey())) {

                String response = callOpenAiCompatible(
                        aiModels.getGroq().getBaseUrl(),
                        aiModels.getGroq().getApiKey(),
                        aiModels.getGroq().getModel(),
                        prompt
                );

                if (isValid(response)) {
                    log.info("AI response received from Groq");
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("Groq failed: {}", e.getMessage());
        }

        // 3. OpenRouter
        try {
            if (aiModels.getOpenrouter().isEnabled()
                    && hasKey(aiModels.getOpenrouter().getApiKey())) {

                String response = callOpenAiCompatible(
                        aiModels.getOpenrouter().getBaseUrl(),
                        aiModels.getOpenrouter().getApiKey(),
                        aiModels.getOpenrouter().getModel(),
                        prompt
                );

                if (isValid(response)) {
                    log.info("AI response received from OpenRouter");
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("OpenRouter failed: {}", e.getMessage());
        }

        // 4. SambaNova
        try {
            if (aiModels.getSambanova().isEnabled()
                    && hasKey(aiModels.getSambanova().getApiKey())) {

                String response = callOpenAiCompatible(
                        aiModels.getSambanova().getBaseUrl(),
                        aiModels.getSambanova().getApiKey(),
                        aiModels.getSambanova().getModel(),
                        prompt
                );

                if (isValid(response)) {
                    log.info("AI response received from SambaNova");
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("SambaNova failed: {}", e.getMessage());
        }

        // 5. Cloudflare
        try {
            if (aiModels.getCloudflare().isEnabled()
                    && hasKey(aiModels.getCloudflare().getApiKey())
                    && hasKey(aiModels.getCloudflare().getAccountId())) {

                String response = callCloudflare(prompt);

                if (isValid(response)) {
                    log.info("AI response received from Cloudflare");
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("Cloudflare failed: {}", e.getMessage());
        }

        throw new RuntimeException(
                "All AI providers are currently unavailable."
        );
    }

    // Gemini API
    private String callGemini(String prompt) throws Exception {

        var config = aiModels.getGemini();

        String url = UriComponentsBuilder
                .fromUriString(config.getBaseUrl())
                .path("/models/{model}:generateContent")
                .queryParam("key", "{key}")
                .buildAndExpand(config.getModel(), config.getApiKey())
                .toUriString();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", maxOutputTokens
                )
        );

        String result = restClient.post()
                .uri(url)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode json = objectMapper.readTree(result);

        return json.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null);
    }

    // Groq, OpenRouter and SambaNova
    private String callOpenAiCompatible(
            String baseUrl,
            String apiKey,
            String model,
            String prompt
    ) throws Exception {

        String url = baseUrl.replaceAll("/+$", "")
                + "/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", temperature,
                "max_tokens", maxOutputTokens
        );

        String result = restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode json = objectMapper.readTree(result);

        return json.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText(null);
    }

    // Cloudflare Workers AI
    private String callCloudflare(String prompt) throws Exception {

        var config = aiModels.getCloudflare();

        String url = config.getBaseUrl().replaceAll("/+$", "")
                + "/accounts/"
                + config.getAccountId()
                + "/ai/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", temperature,
                "max_tokens", maxOutputTokens
        );

        String result = restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode json = objectMapper.readTree(result);

        return json.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText(null);
    }

    // Utility methods
    private boolean hasKey(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isValid(String response) {
        return response != null && !response.isBlank();
    }
}
