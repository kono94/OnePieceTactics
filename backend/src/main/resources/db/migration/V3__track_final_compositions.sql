ALTER TABLE analytics_player_run ADD COLUMN placement_finalized_at INTEGER;
ALTER TABLE analytics_player_run ADD COLUMN final_board_json TEXT;

CREATE INDEX idx_analytics_run_finalized ON analytics_player_run(placement_finalized_at);
CREATE INDEX idx_analytics_run_placement_status ON analytics_player_run(final_placement, status);
CREATE INDEX idx_analytics_match_build_cohort
    ON analytics_match(mode, backend_version, backend_commit, started_at DESC);

UPDATE analytics_player_run
SET final_round = (
        SELECT MAX(pr.round_number)
        FROM analytics_player_round pr
        WHERE pr.run_id = analytics_player_run.id
    )
WHERE EXISTS (
    SELECT 1
    FROM analytics_player_round pr
    WHERE pr.run_id = analytics_player_run.id
);

UPDATE analytics_player_run
SET placement_finalized_at = (
        SELECT pr.captured_at
        FROM analytics_player_round pr
        WHERE pr.run_id = analytics_player_run.id
        ORDER BY pr.round_number DESC
        LIMIT 1
    ),
    final_board_json = (
        SELECT pr.board_json
        FROM analytics_player_round pr
        WHERE pr.run_id = analytics_player_run.id
        ORDER BY pr.round_number DESC
        LIMIT 1
    )
WHERE EXISTS (
    SELECT 1
    FROM analytics_player_round pr
    WHERE pr.run_id = analytics_player_run.id
);
