package net.lwenstrom.tft.backend.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import tools.jackson.databind.json.JsonMapper;

class SqliteGameplayAnalyticsRecorderTest {
    @TempDir
    java.nio.file.Path temporaryDirectory;

    @Test
    void persistsRoundProgressOutcomeAbandonmentAndPlacement() throws Exception {
        var sqliteConfig = new SQLiteConfig();
        sqliteConfig.enforceForeignKeys(true);
        var dataSource = new SQLiteDataSource(sqliteConfig);
        dataSource.setUrl("jdbc:sqlite:" + temporaryDirectory.resolve("analytics.db"));
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        var jdbcTemplate = new JdbcTemplate(dataSource);
        var transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        var recorder = new SqliteGameplayAnalyticsRecorder(
                jdbcTemplate, JsonMapper.builder().build(), transactionTemplate);
        ReflectionTestUtils.setField(recorder, "backendVersion", "1.2.3");
        ReflectionTestUtils.setField(recorder, "backendCommit", "abc123");
        ReflectionTestUtils.setField(recorder, "backendBuildTime", "2026-07-10T20:00:00Z");

        var dataLoader = TestHelpers.createMockDataLoader();
        var player = new Player(
                "Anonymous",
                GameMode.ONEPIECE,
                dataLoader,
                TestHelpers.createSeededRandomProvider(),
                "browser-123",
                "reconnect-hash");
        var bot = new Player("Bot", GameMode.ONEPIECE, dataLoader, TestHelpers.createSeededRandomProvider());
        bot.setBot(true);
        player.addUnitToBoard(TestHelpers.createDefaultUnitDef(), 0, 0, 1);

        recorder.matchStarted("match-key", GameMode.ONEPIECE, 1_000, List.of(player));
        recorder.roundStarted("match-key", 1, 1_100, List.of(player));
        player.takeDamage(12);
        recorder.combatResolved("match-key", 1, 1_200, player.getId(), bot.getId(), false, List.of(player, bot));
        recorder.playerAbandoned("match-key", player.getId(), 1_250);
        player.setPlace(1);
        recorder.matchCompleted("match-key", 1, 1_300, List.of(player));
        recorder.awaitPendingWrites();

        assertThat(jdbcTemplate.queryForObject(
                        "SELECT status FROM analytics_match WHERE room_id = 'match-key'", String.class))
                .isEqualTo("COMPLETED");
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT backend_version FROM analytics_match WHERE room_id = 'match-key'", String.class))
                .isEqualTo("1.2.3");
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT final_placement FROM analytics_player_run WHERE analytics_client_id = 'browser-123'",
                        Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT abandoned_at FROM analytics_player_run WHERE analytics_client_id = 'browser-123'",
                        Long.class))
                .isEqualTo(1_250L);
        assertThat(jdbcTemplate.queryForMap("SELECT * FROM analytics_player_round"))
                .containsEntry("outcome", "WIN");
        assertThat(jdbcTemplate.queryForObject("SELECT post_health FROM analytics_player_round", Integer.class))
                .isEqualTo(88);
        assertThat(jdbcTemplate.queryForObject("SELECT opponent_type FROM analytics_player_round", String.class))
                .isEqualTo("BOT");
        assertThat(jdbcTemplate.queryForObject("SELECT board_json FROM analytics_player_round", String.class))
                .contains("\"definitionId\":\"test-unit-1\"")
                .contains("\"starLevel\":1");
    }

    @Test
    void classifiesBotGhostsAsBotOpponents() throws Exception {
        var sqliteConfig = new SQLiteConfig();
        sqliteConfig.enforceForeignKeys(true);
        var dataSource = new SQLiteDataSource(sqliteConfig);
        dataSource.setUrl("jdbc:sqlite:" + temporaryDirectory.resolve("bot-ghost.db"));
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        var jdbcTemplate = new JdbcTemplate(dataSource);
        var recorder = new SqliteGameplayAnalyticsRecorder(
                jdbcTemplate,
                JsonMapper.builder().build(),
                new TransactionTemplate(new DataSourceTransactionManager(dataSource)));
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = new Player("Anonymous", GameMode.ONEPIECE, dataLoader, TestHelpers.createSeededRandomProvider());
        var bot = new Player("Bot", GameMode.ONEPIECE, dataLoader, TestHelpers.createSeededRandomProvider());
        bot.setBot(true);
        var botGhost = bot.createGhost();

        recorder.matchStarted("match-key", GameMode.ONEPIECE, 1_000, List.of(player));
        recorder.roundStarted("match-key", 1, 1_100, List.of(player));
        recorder.combatResolved(
                "match-key", 1, 1_200, player.getId(), botGhost.getId(), false, List.of(player, botGhost));
        recorder.awaitPendingWrites();

        assertThat(jdbcTemplate.queryForObject("SELECT opponent_type FROM analytics_player_round", String.class))
                .isEqualTo("BOT");
    }
}
