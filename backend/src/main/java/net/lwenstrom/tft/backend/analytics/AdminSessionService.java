package net.lwenstrom.tft.backend.analytics;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSessionService {
    private static final long SESSION_DURATION_MILLIS = 8 * 60 * 60 * 1_000L;
    private static final long LOGIN_WINDOW_MILLIS = 15 * 60 * 1_000L;
    private static final int MAX_FAILED_LOGINS = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final Map<String, LoginFailures> failures = new ConcurrentHashMap<>();

    @Value("${analytics.admin.password:}")
    private String configuredPassword;

    @PostConstruct
    void validateConfiguration() {
        if (configuredPassword.isBlank()) {
            throw new IllegalStateException("analytics.admin.password must not be blank");
        }
    }

    public LoginResult login(String suppliedPassword, String sourceAddress) {
        var now = System.currentTimeMillis();
        var retryAfter = retryAfterSeconds(sourceAddress, now);
        if (retryAfter > 0) {
            return LoginResult.rateLimited(retryAfter);
        }

        if (!MessageDigest.isEqual(
                configuredPassword.getBytes(StandardCharsets.UTF_8),
                suppliedPassword.getBytes(StandardCharsets.UTF_8))) {
            recordFailure(sourceAddress, now);
            return LoginResult.rejected();
        }

        failures.remove(sourceAddress);
        var tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        var token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        var expiresAt = now + SESSION_DURATION_MILLIS;
        sessions.put(hash(token), expiresAt);
        return LoginResult.accepted(token, Instant.ofEpochMilli(expiresAt));
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        var now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> entry.getValue() <= now);
        var expiresAt = sessions.get(hash(token));
        return expiresAt != null && expiresAt > now;
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(hash(token));
        }
    }

    private long retryAfterSeconds(String sourceAddress, long now) {
        var loginFailures = failures.get(sourceAddress);
        if (loginFailures == null || now - loginFailures.windowStartedAt() >= LOGIN_WINDOW_MILLIS) {
            failures.remove(sourceAddress);
            return 0;
        }
        if (loginFailures.count() < MAX_FAILED_LOGINS) {
            return 0;
        }
        return Math.max(1, (loginFailures.windowStartedAt() + LOGIN_WINDOW_MILLIS - now + 999) / 1_000);
    }

    private void recordFailure(String sourceAddress, long now) {
        failures.compute(sourceAddress, (address, previous) -> {
            if (previous == null || now - previous.windowStartedAt() >= LOGIN_WINDOW_MILLIS) {
                return new LoginFailures(now, 1);
            }
            return new LoginFailures(previous.windowStartedAt(), previous.count() + 1);
        });
    }

    private String hash(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record LoginResult(Status status, String accessToken, Instant expiresAt, long retryAfterSeconds) {
        static LoginResult accepted(String accessToken, Instant expiresAt) {
            return new LoginResult(Status.ACCEPTED, accessToken, expiresAt, 0);
        }

        static LoginResult rejected() {
            return new LoginResult(Status.REJECTED, null, null, 0);
        }

        static LoginResult rateLimited(long retryAfterSeconds) {
            return new LoginResult(Status.RATE_LIMITED, null, null, retryAfterSeconds);
        }
    }

    public enum Status {
        ACCEPTED,
        REJECTED,
        RATE_LIMITED
    }

    private record LoginFailures(long windowStartedAt, int count) {}
}
