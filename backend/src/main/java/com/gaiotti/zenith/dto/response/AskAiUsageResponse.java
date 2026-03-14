package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AskAiUsageResponse {
    String mode;
    boolean accessAllowed;
    int perUserPerMinuteLimit;
    int perIpPerMinuteLimit;
    int perUserDailyQuota;
    int perUserCurrentMinuteUsed;
    int perIpCurrentMinuteUsed;
    int perUserDailyUsed;
    int perUserDailyRemaining;
    String note;
}
