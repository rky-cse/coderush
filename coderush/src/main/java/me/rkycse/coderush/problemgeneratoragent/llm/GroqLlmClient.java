package me.rkycse.coderush.problemgeneratoragent.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.rkycse.coderush.problemgeneratoragent.exception.LlmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqLlmClient implements LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(GroqLlmClient.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int JSON_RETRIES = 2;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    @Value("${groq.temperature:0.3}")
    private double temperature;

    @Value("${groq.max-tokens:4096}")
    private int maxTokens;

    public GroqLlmClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Value("${groq.inter-call-delay-ms:3000}")
    private long interCallDelayMs;

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        // Small delay between calls to stay within TPM limits on free tier
        if (interCallDelayMs > 0) {
            try { Thread.sleep(interCallDelayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        Map<String, Object> body = Map.of(
            "model", model,
            "temperature", temperature,
            "max_tokens", maxTokens,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userPrompt)
            )
        );

        // Retry up to 3 times on 429 (rate limit) with increasing backoff
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String response = restClient.post()
                    .uri(GROQ_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

                JsonNode root = objectMapper.readTree(response);
                return root.path("choices").get(0).path("message").path("content").asText();

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    long waitMs = 6000L * attempt; // 6s, 12s, 18s
                    logger.warn("Rate limit hit (attempt {}). Waiting {}ms...", attempt, waitMs);
                    try { Thread.sleep(waitMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    if (attempt == 3) throw new LlmException("Groq rate limit exceeded after 3 retries.", e);
                } else {
                    throw new LlmException("Groq API error " + e.getStatusCode() + ": " + e.getMessage(), e);
                }
            } catch (Exception e) {
                throw new LlmException("Groq API call failed: " + e.getMessage(), e);
            }
        }
        throw new LlmException("Groq API call failed after retries.");
    }

    @Override
    public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
        String enhancedSystem = systemPrompt +
            "\nIMPORTANT: Respond with valid JSON only. No markdown, no text outside JSON." +
            "\nAll string values containing newlines MUST use \\n escape sequences, not literal newlines.";

        String lastError = null;
        for (int attempt = 0; attempt <= JSON_RETRIES; attempt++) {
            String prompt = attempt == 0 ? userPrompt
                : userPrompt + "\n\nYour previous response was not valid JSON. Error: " + lastError
                  + "\nReturn only valid JSON. Use \\n for newlines inside string values.";

            String raw = generate(enhancedSystem, prompt);
            String cleaned = stripFences(raw);
            // Fix literal newlines inside JSON string values (common LLM mistake with code)
            cleaned = fixLiteralNewlinesInJson(cleaned);
            try {
                return objectMapper.readValue(cleaned, responseType);
            } catch (JsonProcessingException e) {
                lastError = e.getMessage();
                logger.warn("JSON parse failed attempt {}: {}", attempt + 1, lastError);
            }
        }
        throw new LlmException("LLM returned invalid JSON after " + (JSON_RETRIES + 1) + " attempts. Last: " + lastError);
    }

    /**
     * Fixes literal newlines inside JSON string values.
     * The LLM sometimes returns code blocks with real newlines instead of \n.
     * We use Jackson's lenient parsing approach: replace literal newlines
     * that appear inside JSON strings with \n escape sequences.
     */
    private String fixLiteralNewlinesInJson(String json) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                result.append(c);
                escaped = false;
            } else if (c == '\\' && inString) {
                result.append(c);
                escaped = true;
            } else if (c == '"') {
                inString = !inString;
                result.append(c);
            } else if (inString && c == '\n') {
                result.append("\\n");
            } else if (inString && c == '\r') {
                result.append("\\r");
            } else if (inString && c == '\t') {
                result.append("\\t");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String stripFences(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            int last = t.lastIndexOf("```");
            if (nl > 0 && last > nl) return t.substring(nl + 1, last).trim();
        }
        return t;
    }
}
