package net.lwenstrom.tft.backend.analytics;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminSessionService sessionService;

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        var result = sessionService.login(
                request.password() == null ? "" : request.password(), servletRequest.getRemoteAddr());
        if (result.status() == AdminSessionService.Status.RATE_LIMITED) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, Long.toString(result.retryAfterSeconds()))
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of("error", "rate_limited"));
        }
        if (result.status() == AdminSessionService.Status.REJECTED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of("error", "invalid_password"));
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new LoginResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            sessionService.logout(token);
        }
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    public record LoginRequest(String password) {}

    public record LoginResponse(String accessToken, Instant expiresAt) {}
}
