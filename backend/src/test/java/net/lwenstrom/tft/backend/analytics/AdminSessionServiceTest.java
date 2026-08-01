package net.lwenstrom.tft.backend.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminSessionServiceTest {
    private AdminSessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new AdminSessionService("tft123");
    }

    @Test
    void exchangesPasswordForRevocableOpaqueToken() {
        var result = sessionService.login("tft123", "127.0.0.1");

        assertThat(result.status()).isEqualTo(AdminSessionService.Status.ACCEPTED);
        assertThat(result.accessToken()).isNotBlank();
        assertThat(sessionService.isValid(result.accessToken())).isTrue();

        sessionService.logout(result.accessToken());
        assertThat(sessionService.isValid(result.accessToken())).isFalse();
    }

    @Test
    void rateLimitsAfterFiveFailedAttempts() {
        for (var attempt = 0; attempt < 5; attempt++) {
            assertThat(sessionService.login("wrong", "192.0.2.1").status())
                    .isEqualTo(AdminSessionService.Status.REJECTED);
        }

        var blocked = sessionService.login("tft123", "192.0.2.1");
        assertThat(blocked.status()).isEqualTo(AdminSessionService.Status.RATE_LIMITED);
        assertThat(blocked.retryAfterSeconds()).isPositive();
    }

    @Test
    void disablesAdminLoginWhenNoPasswordIsConfigured() {
        assertThat(new AdminSessionService("").login("anything", "127.0.0.1").status())
                .isEqualTo(AdminSessionService.Status.REJECTED);
    }
}
