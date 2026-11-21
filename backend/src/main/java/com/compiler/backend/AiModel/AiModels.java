package com.compiler.backend.AiModel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ai.providers")
@Component
@Getter
@Setter
public class AiModels {

    private Gemini gemini = new Gemini();
    private Groq groq = new Groq();
    private Openrouter openrouter = new Openrouter();
    private Sambanova sambanova = new Sambanova();
    private Cloudflare cloudflare = new Cloudflare();


    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String baseUrl;
        private String model;
        private boolean enabled;
    }

    // 2. Groq Configuration
    @Getter
    @Setter
    public static class Groq {
        private String apiKey;
        private String baseUrl;
        private String model;
        private boolean enabled;
    }

    // 3. OpenRouter Configuration
    @Getter
    @Setter
    public static class Openrouter {
        private String apiKey;
        private String baseUrl;
        private String model;
        private boolean enabled;
    }

    // 4. SambaNova Configuration
    @Getter
    @Setter
    public static class Sambanova {
        private String apiKey;
        private String baseUrl;
        private String model;
        private boolean enabled;
    }

    // 5. Cloudflare Configuration
    @Getter
    @Setter
    public static class Cloudflare {
        private String apiKey;
        private String baseUrl;
        private String model;
        private String accountId;
        private boolean enabled;
    }

}
