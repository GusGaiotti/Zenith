package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AskAiResponse {
    String answer;
    ContextLevel contextLevelUsed;
    String disclaimer;

    public enum ContextLevel {
        SUMMARY,
        EXTENDED,
        SAMPLED_TRANSACTIONS
    }
}
