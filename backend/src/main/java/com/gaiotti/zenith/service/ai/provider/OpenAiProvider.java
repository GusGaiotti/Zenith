package com.gaiotti.zenith.service.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaiotti.zenith.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiProvider implements AiProvider {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public AiProviderResult ask(String systemPrompt, String userPrompt, int maxResponseTokens) {
        if (aiProperties.getOpenai().getApiKey() == null || aiProperties.getOpenai().getApiKey().isBlank()) {
            throw new AiProviderException("OpenAI API key not configured");
        }

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(aiProperties.getTimeoutMs());
            factory.setReadTimeout(aiProperties.getTimeoutMs());
            RestTemplate restTemplate = new RestTemplate(factory);

            String baseUrl = aiProperties.getOpenai().getBaseUrl().replaceAll("/+$", "");
            String endpoint = baseUrl + "/chat/completions";

            Map<String, Object> body = Map.of(
                    "model", aiProperties.getOpenai().getModel(),
                    "temperature", 0.2,
                    "max_tokens", maxResponseTokens,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getOpenai().getApiKey());

            String rawResponse = restTemplate.postForObject(
                    endpoint,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiProviderException("OpenAI returned empty choices");
            }

            String answer = choices.get(0).path("message").path("content").asText("").trim();
            if (answer.isBlank()) {
                throw new AiProviderException("OpenAI returned empty response");
            }

            return new AiProviderResult(answer, name());
        } catch (AiProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiProviderException("OpenAI provider request failed", ex);
        }
    }
}
