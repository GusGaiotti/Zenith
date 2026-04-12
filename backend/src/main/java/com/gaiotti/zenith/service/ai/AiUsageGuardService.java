package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.config.AiProperties;
import com.gaiotti.zenith.exception.QuotaExceededException;
import com.gaiotti.zenith.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AiUsageGuardService {

    private final AiProperties aiProperties;

    private final ConcurrentHashMap<String, WindowCounter> userRateCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WindowCounter> ipRateCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, DailyCounter> userDailyQuota = new ConcurrentHashMap<>();

    private static final long RATE_WINDOW_MS = 60_000L;
    private static final int CLEANUP_INTERVAL = 200;
    private final AtomicInteger assertCallCount = new AtomicInteger(0);

    public void assertAllowedAndConsume(Long userId, String clientIp) {
        if (!aiProperties.getLimits().isEnabled()) {
            return;
        }

        long now = Instant.now().toEpochMilli();

        checkWindowCounter(userRateCounters, "u:" + userId, aiProperties.getLimits().getPerUserPerMinute(), now, RATE_WINDOW_MS,
                "AI rate limit exceeded for this user");
        checkWindowCounter(ipRateCounters, "ip:" + clientIp, aiProperties.getLimits().getPerIpPerMinute(), now, RATE_WINDOW_MS,
                "AI rate limit exceeded for this IP");
        checkDailyQuota(userId, aiProperties.getLimits().getPerUserDailyQuota());

        if (assertCallCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            cleanupStaleEntries(now, RATE_WINDOW_MS);
        }
    }

    private void cleanupStaleEntries(long now, long windowMs) {
        LocalDate today = LocalDate.now();
        userRateCounters.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return now - entry.getValue().windowStart >= windowMs * 2;
            }
        });
        ipRateCounters.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return now - entry.getValue().windowStart >= windowMs * 2;
            }
        });
        userDailyQuota.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return entry.getValue().day.isBefore(today);
            }
        });
    }

    public UsageSnapshot getSnapshot(Long userId, String clientIp) {
        int perUserMinuteUsed = getWindowCount(userRateCounters, "u:" + userId);
        int perIpMinuteUsed = getWindowCount(ipRateCounters, "ip:" + clientIp);
        int dailyUsed = getDailyCount(userId);
        int dailyLimit = Math.max(1, aiProperties.getLimits().getPerUserDailyQuota());

        return new UsageSnapshot(
                perUserMinuteUsed,
                perIpMinuteUsed,
                dailyUsed,
                Math.max(0, dailyLimit - dailyUsed)
        );
    }

    private void checkWindowCounter(
            ConcurrentHashMap<String, WindowCounter> counters,
            String key,
            int limit,
            long now,
            long windowMs,
            String errorMessage
    ) {
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(now));
        synchronized (counter) {
            if (now - counter.windowStart >= windowMs) {
                counter.windowStart = now;
                counter.count = 0;
            }
            if (counter.count >= Math.max(1, limit)) {
                throw new RateLimitExceededException(errorMessage);
            }
            counter.count++;
        }
    }

    private void checkDailyQuota(Long userId, int dailyLimit) {
        LocalDate today = LocalDate.now();
        DailyCounter counter = userDailyQuota.computeIfAbsent(userId, ignored -> new DailyCounter(today));

        synchronized (counter) {
            if (!counter.day.equals(today)) {
                counter.day = today;
                counter.count = 0;
            }
            if (counter.count >= Math.max(1, dailyLimit)) {
                throw new QuotaExceededException("Daily AI quota exceeded for this user");
            }
            counter.count++;
        }
    }

    private int getWindowCount(ConcurrentHashMap<String, WindowCounter> counters, String key) {
        WindowCounter counter = counters.get(key);
        if (counter == null) {
            return 0;
        }

        long now = Instant.now().toEpochMilli();
        synchronized (counter) {
            if (now - counter.windowStart >= RATE_WINDOW_MS) {
                counter.windowStart = now;
                counter.count = 0;
            }
            return counter.count;
        }
    }

    private int getDailyCount(Long userId) {
        DailyCounter counter = userDailyQuota.get(userId);
        if (counter == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        synchronized (counter) {
            if (!counter.day.equals(today)) {
                counter.day = today;
                counter.count = 0;
            }
            return counter.count;
        }
    }

    private static final class WindowCounter {
        private long windowStart;
        private int count;

        private WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = 0;
        }
    }

    private static final class DailyCounter {
        private LocalDate day;
        private int count;

        private DailyCounter(LocalDate day) {
            this.day = day;
            this.count = 0;
        }
    }

    public record UsageSnapshot(
            int perUserCurrentMinuteUsed,
            int perIpCurrentMinuteUsed,
            int perUserDailyUsed,
            int perUserDailyRemaining
    ) {}
}
