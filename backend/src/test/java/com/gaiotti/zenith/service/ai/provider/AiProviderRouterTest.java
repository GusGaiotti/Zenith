package com.gaiotti.zenith.service.ai.provider;

import com.gaiotti.zenith.config.AiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiProviderRouterTest {

    private AiProperties aiProperties;
    private AiProviderRouter router;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        router = new AiProviderRouter(
                aiProperties,
                new OffAiProvider(),
                new OllamaAiProvider(aiProperties, new com.fasterxml.jackson.databind.ObjectMapper()),
                new OpenAiProvider(aiProperties, new com.fasterxml.jackson.databind.ObjectMapper())
        );
    }

    @Test
    void resolveActiveProvider_LocalModeUsesOllama() {
        aiProperties.setMode("local");
        assertThat(router.resolveActiveProvider().name()).isEqualTo("ollama");
    }

    @Test
    void resolveActiveProvider_OpenAiModeUsesOpenAi() {
        aiProperties.setMode("openai");
        assertThat(router.resolveActiveProvider().name()).isEqualTo("openai");
    }

    @Test
    void resolveActiveProvider_UnknownModeFallsBackToOff() {
        aiProperties.setMode("something-else");
        assertThat(router.resolveActiveProvider().name()).isEqualTo("off");
    }
}
