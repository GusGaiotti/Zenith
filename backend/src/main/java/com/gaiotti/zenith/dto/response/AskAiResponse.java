package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AskAiResponse {
    String headline;
    String answer;
    java.util.List<String> highlights;
    java.util.List<String> recommendedActions;
    ContextLevel contextLevelUsed;
    String disclaimer;

    public enum ContextLevel {
        SUMMARY,
        EXTENDED,
        SAMPLED_TRANSACTIONS
    }
}
