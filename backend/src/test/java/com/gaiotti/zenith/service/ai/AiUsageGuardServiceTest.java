package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.config.AiProperties;
import com.gaiotti.zenith.exception.QuotaExceededException;
import com.gaiotti.zenith.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiUsageGuardServiceTest {

    private AiProperties aiProperties;
    private AiUsageGuardService service;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.getLimits().setPerUserPerMinute(2);
        aiProperties.getLimits().setPerIpPerMinute(2);
        aiProperties.getLimits().setPerUserDailyQuota(2);
        service = new AiUsageGuardService(aiProperties);
    }

    @Test
    void assertAllowedAndConsume_RateLimitExceededForUser_Throws429() {
        service.assertAllowedAndConsume(1L, "127.0.0.1");
        service.assertAllowedAndConsume(1L, "127.0.0.1");

        assertThatThrownBy(() -> service.assertAllowedAndConsume(1L, "127.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void assertAllowedAndConsume_QuotaExceeded_Throws429() {
        aiProperties.getLimits().setPerUserPerMinute(10);
        aiProperties.getLimits().setPerIpPerMinute(10);
        service = new AiUsageGuardService(aiProperties);

        service.assertAllowedAndConsume(1L, "127.0.0.1");
        service.assertAllowedAndConsume(1L, "127.0.0.1");

        assertThatThrownBy(() -> service.assertAllowedAndConsume(1L, "127.0.0.1"))
                .isInstanceOf(QuotaExceededException.class);
    }
}
