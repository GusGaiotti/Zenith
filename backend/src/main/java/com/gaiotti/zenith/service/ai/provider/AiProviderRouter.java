package com.gaiotti.zenith.service.ai.provider;

import com.gaiotti.zenith.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiProviderRouter {

    private final AiProperties aiProperties;
    private final OffAiProvider offAiProvider;
    private final OllamaAiProvider ollamaAiProvider;
    private final OpenAiProvider openAiProvider;

    public AiProvider resolveActiveProvider() {
        String mode = aiProperties.getMode() == null ? "off" : aiProperties.getMode().trim().toLowerCase();

        return switch (mode) {
            case "local", "ollama" -> ollamaAiProvider;
            case "openai" -> openAiProvider;
            default -> offAiProvider;
        };
    }
}
