package net.lwenstrom.tft.backend.analytics;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class AnalyticsSecurityConfiguration {
    private final AdminSessionService sessionService;

    @Bean
    SecurityFilterChain analyticsSecurityFilterChain(HttpSecurity http) throws Exception {
        var bearerFilter = new AdminBearerTokenFilter(sessionService);
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/admin/auth/login")
                        .permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasRole("ANALYTICS_ADMIN")
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setHeader("Cache-Control", "no-store");
                    response.getWriter().write("{\"error\":\"unauthorized\"}");
                }))
                .addFilterBefore(bearerFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
