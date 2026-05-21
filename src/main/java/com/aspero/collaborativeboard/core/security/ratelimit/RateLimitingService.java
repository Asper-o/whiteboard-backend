package com.aspero.collaborativeboard.core.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // This map stores the IP address and its personal stamina bucket
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ipAddress) {
        return cache.computeIfAbsent(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String ipAddress) {
        // The Rules: Max 5 attempts. Refills all 5 attempts every 15 minutes.
        Refill refill = Refill.intervally(5, Duration.ofMinutes(15));
        Bandwidth limit = Bandwidth.classic(5, refill);
                
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}