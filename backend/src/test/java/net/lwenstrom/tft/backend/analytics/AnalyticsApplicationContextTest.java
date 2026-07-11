package net.lwenstrom.tft.backend.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import net.lwenstrom.tft.backend.core.analytics.GameplayAnalyticsRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:sqlite:file:analytics-context-test?mode=memory&cache=shared&foreign_keys=on",
            "analytics.admin.password=tft123"
        })
class AnalyticsApplicationContextTest {
    @Autowired
    private GameplayAnalyticsRecorder analyticsRecorder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void startsWithMigratedSqliteAndAdminSecurity() {
        assertThat(analyticsRecorder).isInstanceOf(SqliteGameplayAnalyticsRecorder.class);
        assertThat(securityFilterChain).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'analytics_match'",
                        Integer.class))
                .isEqualTo(1);
    }
}
