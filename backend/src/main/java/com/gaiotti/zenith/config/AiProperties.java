package com.gaiotti.zenith.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai")
@Getter
@Setter
public class AiProperties {

    private String mode = "off";
    private int timeoutMs = 8000;
    private int maxResponseTokens = 300;

    private Ollama ollama = new Ollama();
    private OpenAi openai = new OpenAi();
    private Access access = new Access();
    private Limits limits = new Limits();

    @Getter
    @Setter
    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.1:8b";
    }

    @Getter
    @Setter
    public static class OpenAi {
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey = "";
        private String model = "gpt-4o-mini";
    }

    @Getter
    @Setter
    public static class Access {
        private String productionAllowlistEmails = "";
    }

    @Getter
    @Setter
    public static class Limits {
        private boolean enabled = true;
        private int perUserPerMinute = 8;
        private int perIpPerMinute = 20;
        private int perUserDailyQuota = 50;
    }
}
