package net.lwenstrom.tft.backend.analytics;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Repository
@RequiredArgsConstructor
public class AdminAnalyticsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper objectMapper;

    public Summary summary(long from, long to, String mode) {
        var filter = " FROM analytics_match m WHERE m.started_at >= ? AND m.started_at < ?"
                + (mode == null ? "" : " AND m.mode = ?");
        var parameters = mode == null ? new Object[] {from, to} : new Object[] {from, to, mode};
        var matchStats = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) games_started,"
                        + " SUM(CASE WHEN m.status = 'COMPLETED' THEN 1 ELSE 0 END) games_completed,"
                        + " SUM(CASE WHEN m.status = 'INTERRUPTED' THEN 1 ELSE 0 END) games_interrupted,"
                        + " AVG(m.final_round) average_final_round, MAX(m.final_round) max_final_round"
                        + filter,
                parameters);
        var runStats = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) human_runs, SUM(CASE WHEN r.abandoned_at IS NOT NULL THEN 1 ELSE 0 END) abandoned"
                        + " FROM analytics_player_run r JOIN analytics_match m ON m.id = r.match_id"
                        + " WHERE m.started_at >= ? AND m.started_at < ?"
                        + (mode == null ? "" : " AND m.mode = ?"),
                parameters);

        var humanRuns = number(runStats.get("human_runs")).longValue();
        var abandoned = number(runStats.get("abandoned")).longValue();
        return new Summary(
                number(matchStats.get("games_started")).longValue(),
                number(matchStats.get("games_completed")).longValue(),
                number(matchStats.get("games_interrupted")).longValue(),
                humanRuns,
                abandoned,
                humanRuns == 0 ? 0 : (double) abandoned / humanRuns,
                number(matchStats.get("average_final_round")).doubleValue(),
                number(matchStats.get("max_final_round")).intValue(),
                groupedCounts("SELECT m.mode label, COUNT(*) count" + filter + " GROUP BY m.mode", parameters),
                groupedCounts(
                        "SELECT CAST(r.final_placement AS TEXT) label, COUNT(*) count"
                                + " FROM analytics_player_run r JOIN analytics_match m ON m.id = r.match_id"
                                + " WHERE m.started_at >= ? AND m.started_at < ? AND r.final_placement IS NOT NULL"
                                + (mode == null ? "" : " AND m.mode = ?")
                                + " GROUP BY r.final_placement ORDER BY r.final_placement",
                        parameters),
                groupedCounts(
                        "SELECT pr.outcome label, COUNT(*) count FROM analytics_player_round pr"
                                + " JOIN analytics_player_run r ON r.id = pr.run_id"
                                + " JOIN analytics_match m ON m.id = r.match_id"
                                + " WHERE m.started_at >= ? AND m.started_at < ? AND pr.outcome IS NOT NULL"
                                + (mode == null ? "" : " AND m.mode = ?")
                                + " GROUP BY pr.outcome",
                        parameters),
                botRoundOutcomes(from, to, mode));
    }

    public RunsPage runs(
            long from, long to, String mode, String analyticsClientId, Boolean abandoned, Cursor cursor, int size) {
        var sql = new StringBuilder("SELECT r.*, m.room_id, m.mode, m.status match_status, m.ended_at,"
                + " SUM(CASE WHEN pr.outcome = 'WIN' THEN 1 ELSE 0 END) wins,"
                + " SUM(CASE WHEN pr.outcome = 'LOSS' THEN 1 ELSE 0 END) losses,"
                + " SUM(CASE WHEN pr.outcome = 'DRAW' THEN 1 ELSE 0 END) draws"
                + " FROM analytics_player_run r JOIN analytics_match m ON m.id = r.match_id"
                + " LEFT JOIN analytics_player_round pr ON pr.run_id = r.id"
                + " WHERE m.started_at >= ? AND m.started_at < ?");
        var parameters = new ArrayList<>();
        parameters.add(from);
        parameters.add(to);
        if (mode != null) {
            sql.append(" AND m.mode = ?");
            parameters.add(mode);
        }
        if (analyticsClientId != null && !analyticsClientId.isBlank()) {
            sql.append(" AND r.analytics_client_id = ?");
            parameters.add(analyticsClientId);
        }
        if (abandoned != null) {
            sql.append(abandoned ? " AND r.abandoned_at IS NOT NULL" : " AND r.abandoned_at IS NULL");
        }
        if (cursor != null) {
            sql.append(" AND (r.started_at < ? OR (r.started_at = ? AND r.id < ?))");
            parameters.add(cursor.startedAt());
            parameters.add(cursor.startedAt());
            parameters.add(cursor.runId());
        }
        sql.append(" GROUP BY r.id ORDER BY r.started_at DESC, r.id DESC LIMIT ?");
        parameters.add(size + 1);
        var rows = jdbcTemplate.query(sql.toString(), this::mapRun, parameters.toArray());
        var hasMore = rows.size() > size;
        var items = hasMore ? rows.subList(0, size) : rows;
        var last = hasMore ? items.getLast() : null;
        return new RunsPage(
                items, last == null ? null : encodeCursor(last.startedAt().toEpochMilli(), last.runId()));
    }

    public RunDetail runDetail(String runId) {
        var runs = jdbcTemplate.query(
                "SELECT r.*, m.room_id, m.mode, m.status match_status, m.ended_at,"
                        + " SUM(CASE WHEN pr.outcome = 'WIN' THEN 1 ELSE 0 END) wins,"
                        + " SUM(CASE WHEN pr.outcome = 'LOSS' THEN 1 ELSE 0 END) losses,"
                        + " SUM(CASE WHEN pr.outcome = 'DRAW' THEN 1 ELSE 0 END) draws"
                        + " FROM analytics_player_run r JOIN analytics_match m ON m.id = r.match_id"
                        + " LEFT JOIN analytics_player_round pr ON pr.run_id = r.id WHERE r.id = ? GROUP BY r.id",
                this::mapRun,
                runId);
        if (runs.isEmpty()) {
            return null;
        }
        var rounds = jdbcTemplate.query(
                "SELECT * FROM analytics_player_round WHERE run_id = ? ORDER BY round_number", this::mapRound, runId);
        return new RunDetail(runs.getFirst(), rounds);
    }

    public Cursor decodeCursor(String encoded) {
        try {
            var decoded = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            var separator = decoded.indexOf('|');
            if (separator <= 0 || separator == decoded.length() - 1) {
                throw new IllegalArgumentException("Invalid cursor");
            }
            return new Cursor(Long.parseLong(decoded.substring(0, separator)), decoded.substring(separator + 1));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid cursor", exception);
        }
    }

    private String encodeCursor(long startedAt, String runId) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((startedAt + "|" + runId).getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Long> groupedCounts(String sql, Object[] parameters) {
        var result = new LinkedHashMap<String, Long>();
        jdbcTemplate.query(
                sql,
                resultSet -> {
                    result.put(resultSet.getString("label"), resultSet.getLong("count"));
                },
                parameters);
        return result;
    }

    private RunSummary mapRun(ResultSet resultSet, int rowNumber) throws SQLException {
        return new RunSummary(
                resultSet.getString("id"),
                resultSet.getString("match_id"),
                resultSet.getString("analytics_client_id"),
                resultSet.getString("mode"),
                resultSet.getString("status"),
                instant(resultSet, "started_at"),
                nullableInstant(resultSet, "abandoned_at"),
                nullableInteger(resultSet, "final_placement"),
                nullableInteger(resultSet, "final_health"),
                nullableInteger(resultSet, "final_round"),
                resultSet.getInt("wins"),
                resultSet.getInt("losses"),
                resultSet.getInt("draws"));
    }

    private RoundSnapshot mapRound(ResultSet resultSet, int rowNumber) throws SQLException {
        return new RoundSnapshot(
                resultSet.getInt("round_number"),
                instant(resultSet, "captured_at"),
                nullableInstant(resultSet, "resolved_at"),
                resultSet.getInt("pre_health"),
                nullableInteger(resultSet, "post_health"),
                resultSet.getInt("gold"),
                resultSet.getInt("player_level"),
                resultSet.getInt("xp"),
                parseJson(resultSet.getString("board_json")),
                parseJson(resultSet.getString("augments_json")),
                resultSet.getString("outcome"),
                resultSet.getString("opponent_type"));
    }

    private List<BotRoundOutcome> botRoundOutcomes(long from, long to, String mode) {
        var sql = "SELECT pr.round_number,"
                + " SUM(CASE WHEN pr.outcome = 'WIN' THEN 1 ELSE 0 END) wins,"
                + " SUM(CASE WHEN pr.outcome = 'LOSS' THEN 1 ELSE 0 END) losses,"
                + " SUM(CASE WHEN pr.outcome = 'DRAW' THEN 1 ELSE 0 END) draws"
                + " FROM analytics_player_round pr"
                + " JOIN analytics_player_run r ON r.id = pr.run_id"
                + " JOIN analytics_match m ON m.id = r.match_id"
                + " WHERE m.started_at >= ? AND m.started_at < ?"
                + " AND pr.opponent_type = 'BOT' AND pr.outcome IS NOT NULL"
                + (mode == null ? "" : " AND m.mode = ?")
                + " GROUP BY pr.round_number ORDER BY pr.round_number";
        var parameters = mode == null ? new Object[] {from, to} : new Object[] {from, to, mode};
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new BotRoundOutcome(
                        resultSet.getInt("round_number"),
                        resultSet.getLong("wins"),
                        resultSet.getLong("losses"),
                        resultSet.getLong("draws")),
                parameters);
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Invalid analytics JSON", exception);
        }
    }

    private Number number(Object value) {
        return value instanceof Number number ? number : 0;
    }

    private Instant instant(ResultSet resultSet, String column) throws SQLException {
        return Instant.ofEpochMilli(resultSet.getLong(column));
    }

    private Instant nullableInstant(ResultSet resultSet, String column) throws SQLException {
        var value = resultSet.getObject(column);
        return value == null ? null : Instant.ofEpochMilli(((Number) value).longValue());
    }

    private Integer nullableInteger(ResultSet resultSet, String column) throws SQLException {
        var value = resultSet.getObject(column);
        return value == null ? null : ((Number) value).intValue();
    }

    public record Summary(
            long gamesStarted,
            long gamesCompleted,
            long gamesInterrupted,
            long humanRuns,
            long abandonmentCount,
            double abandonmentRate,
            double averageFinalRound,
            int maxFinalRound,
            Map<String, Long> modeCounts,
            Map<String, Long> placementDistribution,
            Map<String, Long> outcomeDistribution,
            List<BotRoundOutcome> botRoundOutcomes) {}

    public record BotRoundOutcome(int round, long wins, long losses, long draws) {}

    public record RunsPage(List<RunSummary> items, String nextCursor) {}

    public record RunDetail(RunSummary run, List<RoundSnapshot> rounds) {}

    public record RunSummary(
            String runId,
            String matchId,
            String anonymousPlayerId,
            String mode,
            String status,
            Instant startedAt,
            Instant abandonedAt,
            Integer finalPlacement,
            Integer finalHealth,
            Integer finalRound,
            int wins,
            int losses,
            int draws) {}

    public record RoundSnapshot(
            int round,
            Instant capturedAt,
            Instant resolvedAt,
            int healthBefore,
            Integer healthAfter,
            int gold,
            int level,
            int xp,
            JsonNode board,
            JsonNode augments,
            String outcome,
            String opponentType) {}

    public record Cursor(long startedAt, String runId) {}
}
