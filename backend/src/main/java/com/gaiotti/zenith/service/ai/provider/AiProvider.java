package com.gaiotti.zenith.service.ai.provider;

public interface AiProvider {
    String name();

    AiProviderResult ask(String systemPrompt, String userPrompt, int maxResponseTokens);
}
