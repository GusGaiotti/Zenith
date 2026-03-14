package com.gaiotti.zenith.service.ai.provider;

import org.springframework.stereotype.Component;

@Component
public class OffAiProvider implements AiProvider {

    @Override
    public String name() {
        return "off";
    }

    @Override
    public AiProviderResult ask(String systemPrompt, String userPrompt, int maxResponseTokens) {
        throw new AiProviderException("AI provider is disabled");
    }
}
