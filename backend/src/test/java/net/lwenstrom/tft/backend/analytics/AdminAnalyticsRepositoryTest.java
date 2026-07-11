package net.lwenstrom.tft.backend.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import tools.jackson.databind.json.JsonMapper;

class AdminAnalyticsRepositoryTest {
    @TempDir
    java.nio.file.Path temporaryDirectory;

    private JdbcTemplate jdbcTemplate;
    private AdminAnalyticsRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        var database = Files.createFile(temporaryDirectory.resolve("analytics.db"));
        var config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        var dataSource = new SQLiteDataSource(config);
        dataSource.setUrl("jdbc:sqlite:" + database);
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository =
                new AdminAnalyticsRepository(jdbcTemplate, JsonMapper.builder().build());
    }

    @Test
    void aggregatesAndPagesRuns() {
        insertCompletedRun("run-2", "match-2", 2_000, true, "LOSS", "BOT");
        insertCompletedRun("run-1", "match-1", 1_000, false, "WIN", "HUMAN");

        var summary = repository.summary(0, 3_000, null);
        assertThat(summary.gamesStarted()).isEqualTo(2);
        assertThat(summary.gamesCompleted()).isEqualTo(2);
        assertThat(summary.humanRuns()).isEqualTo(2);
        assertThat(summary.abandonmentCount()).isEqualTo(1);
        assertThat(summary.outcomeDistribution()).containsEntry("WIN", 1L).containsEntry("LOSS", 1L);
        assertThat(summary.botRoundOutcomes())
                .containsExactly(new AdminAnalyticsRepository.BotRoundOutcome(4, 0, 1, 0));

        var firstPage = repository.runs(0, 3_000, null, null, null, null, 1);
        assertThat(firstPage.items())
                .extracting(AdminAnalyticsRepository.RunSummary::runId)
                .containsExactly("run-2");
        assertThat(firstPage.nextCursor()).isNotBlank();

        var secondPage =
                repository.runs(0, 3_000, null, null, null, repository.decodeCursor(firstPage.nextCursor()), 1);
        assertThat(secondPage.items())
                .extracting(AdminAnalyticsRepository.RunSummary::runId)
                .containsExactly("run-1");
        assertThat(repository.runDetail("run-2").rounds()).hasSize(1);
    }

    private void insertCompletedRun(
            String runId, String matchId, long startedAt, boolean abandoned, String outcome, String opponentType) {
        jdbcTemplate.update(
                "INSERT INTO analytics_match"
                        + " (id, room_id, mode, started_at, ended_at, final_round, status)"
                        + " VALUES (?, ?, 'ONE_PIECE', ?, ?, 4, 'COMPLETED')",
                matchId,
                UUID.randomUUID().toString(),
                startedAt,
                startedAt + 100);
        jdbcTemplate.update(
                "INSERT INTO analytics_player_run"
                        + " (id, match_id, player_id, analytics_client_id, started_at, abandoned_at,"
                        + " final_placement, final_health, final_round, status)"
                        + " VALUES (?, ?, ?, ?, ?, ?, 2, 10, 4, 'COMPLETED')",
                runId,
                matchId,
                UUID.randomUUID().toString(),
                "browser-" + runId,
                startedAt,
                abandoned ? startedAt + 50 : null);
        jdbcTemplate.update(
                "INSERT INTO analytics_player_round"
                        + " (id, run_id, round_number, captured_at, resolved_at, pre_health, gold, player_level, xp,"
                        + " board_json, augments_json, outcome, opponent_type, post_health)"
                        + " VALUES (?, ?, 4, ?, ?, 20, 10, 3, 2, '[]', '[]', ?, ?, 10)",
                UUID.randomUUID().toString(),
                runId,
                startedAt,
                startedAt + 10,
                outcome,
                opponentType);
    }
}
