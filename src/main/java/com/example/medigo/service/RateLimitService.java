package com.example.medigo.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 3600; // 1 hora

    private final Map<Long, UserRateLimit> userLimits = new ConcurrentHashMap<>();

    public boolean isAllowed(Long userId) {
        UserRateLimit limit = userLimits.computeIfAbsent(userId, k -> new UserRateLimit());
        return limit.tryConsume();
    }

    public int getRemainingRequests(Long userId) {
        UserRateLimit limit = userLimits.get(userId);
        return limit != null ? limit.getRemaining() : MAX_REQUESTS;
    }

    public long getTimeUntilReset(Long userId) {
        UserRateLimit limit = userLimits.get(userId);
        return limit != null ? limit.getTimeUntilReset() : 0;
    }

    @Data
    private static class UserRateLimit {
        private int count = 0;
        private long windowStart = Instant.now().getEpochSecond();

        public boolean tryConsume() {
            long now = Instant.now().getEpochSecond();
            
            // Reset si ha pasado la ventana de tiempo
            if (now - windowStart >= WINDOW_SECONDS) {
                count = 0;
                windowStart = now;
            }

            if (count < MAX_REQUESTS) {
                count++;
                return true;
            }
            
            return false;
        }

        public int getRemaining() {
            long now = Instant.now().getEpochSecond();
            if (now - windowStart >= WINDOW_SECONDS) {
                return MAX_REQUESTS;
            }
            return Math.max(0, MAX_REQUESTS - count);
        }

        public long getTimeUntilReset() {
            long now = Instant.now().getEpochSecond();
            long elapsed = now - windowStart;
            return Math.max(0, WINDOW_SECONDS - elapsed);
        }
    }
}
