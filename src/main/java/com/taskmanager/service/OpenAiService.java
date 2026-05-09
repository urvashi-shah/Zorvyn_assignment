package com.taskmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.model.AiSuggestion;
import com.taskmanager.model.TaskPriority;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Beginner note:
 * This service calls OpenAI's REST API and asks it to return a small JSON we can store.
 */
@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public OpenAiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.openai.base-url}") String baseUrl,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.model}") String model,
            @Value("${app.openai.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AiEnrichment enrichTaskDescription(String description) {
        // If no key is configured, we keep the project runnable and just return a simple default.
        if (apiKey == null || apiKey.isBlank()) {
            return fallback();
        }

        // We ask the model to produce JSON so it's easy to parse.
        String system = "You are an assistant that classifies tasks. Respond ONLY as JSON.";
        String user = """
                Task description:
                %s

                Return JSON with keys:
                - priority: one of HIGH, MEDIUM, LOW
                - summary: short summary (max 1 sentence)
                - firstStep: suggestion for what to do first (max 1 sentence)
                """.formatted(description == null ? "" : description);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                },
                "temperature", 0.2,
                // Ask OpenAI to strictly return JSON content.
                "response_format", Map.of("type", "json_object")
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> resp = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions",
                    entity,
                    JsonNode.class
            );

            JsonNode response = resp.getBody();
            if (response == null) {
                return fallback();
            }

            String content = response.at("/choices/0/message/content").asText(null);
            if (content == null || content.isBlank()) {
                return fallback();
            }

            // Some models wrap JSON in ```json ... ```; normalize before parsing.
            JsonNode json = objectMapper.readTree(normalizeJsonText(content));

            TaskPriority priority = TaskPriority.MEDIUM;
            String p = json.path("priority").asText("MEDIUM").toUpperCase();
            try {
                priority = TaskPriority.valueOf(p);
            } catch (Exception ignored) {
                // keep MEDIUM
            }

            String summary = json.path("summary").asText("");
            String firstStep = json.path("firstStep").asText("");

            if (summary.isBlank()) {
                summary = "Plan this task in one short step-by-step list.";
            }
            if (firstStep.isBlank()) {
                firstStep = "Start with the smallest actionable step.";
            }

            return new AiEnrichment(priority, new AiSuggestion(summary, firstStep));
        } catch (Exception ex) {
            log.warn("OpenAI enrich failed, using fallback. Reason: {}", ex.getMessage());
            return fallback();
        }
    }

    /**
     * Beginner note:
     * Handles common LLM output styles so JSON parsing does not fail.
     */
    private String normalizeJsonText(String raw) {
        String text = raw.trim();

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }

        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1);
        }

        return text.trim();
    }

    private AiEnrichment fallback() {
        return new AiEnrichment(
                TaskPriority.MEDIUM,
                new AiSuggestion("No AI summary available.", "Start by breaking the task into smaller steps.")
        );
    }

    public record AiEnrichment(TaskPriority priority, AiSuggestion suggestion) {}
}

