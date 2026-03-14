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

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OllamaAiProvider implements AiProvider {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return "ollama";
    }

    @Override
    public AiProviderResult ask(String systemPrompt, String userPrompt, int maxResponseTokens) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(aiProperties.getTimeoutMs());
            factory.setReadTimeout(aiProperties.getTimeoutMs());
            RestTemplate restTemplate = new RestTemplate(factory);

            String baseUrl = aiProperties.getOllama().getBaseUrl().replaceAll("/+$", "");
            String endpoint = baseUrl + "/api/generate";

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", aiProperties.getOllama().getModel());
            body.put("prompt", userPrompt);
            body.put("system", systemPrompt);
            body.put("stream", false);
            body.put("options", Map.of("num_predict", maxResponseTokens));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String rawResponse = restTemplate.postForObject(
                    endpoint,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(rawResponse);
            String answer = root.path("response").asText("").trim();
            if (answer.isBlank()) {
                throw new AiProviderException("Ollama returned empty response");
            }
            return new AiProviderResult(answer, name());
        } catch (AiProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiProviderException("Ollama provider request failed", ex);
        }
    }
}
