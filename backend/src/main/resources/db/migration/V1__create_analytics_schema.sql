CREATE TABLE analytics_match (
    id TEXT PRIMARY KEY,
    room_id TEXT NOT NULL UNIQUE,
    mode TEXT NOT NULL,
    backend_version TEXT,
    backend_commit TEXT,
    backend_build_time TEXT,
    started_at INTEGER NOT NULL,
    ended_at INTEGER,
    final_round INTEGER,
    status TEXT NOT NULL CHECK (status IN ('STARTED', 'COMPLETED', 'INTERRUPTED'))
);

CREATE TABLE analytics_player_run (
    id TEXT PRIMARY KEY,
    match_id TEXT NOT NULL REFERENCES analytics_match(id) ON DELETE CASCADE,
    player_id TEXT NOT NULL,
    analytics_client_id TEXT,
    started_at INTEGER NOT NULL,
    abandoned_at INTEGER,
    final_placement INTEGER,
    final_health INTEGER,
    final_round INTEGER,
    status TEXT NOT NULL CHECK (status IN ('STARTED', 'COMPLETED', 'INTERRUPTED')),
    UNIQUE (match_id, player_id)
);

CREATE TABLE analytics_player_round (
    id TEXT PRIMARY KEY,
    run_id TEXT NOT NULL REFERENCES analytics_player_run(id) ON DELETE CASCADE,
    round_number INTEGER NOT NULL,
    captured_at INTEGER NOT NULL,
    resolved_at INTEGER,
    pre_health INTEGER NOT NULL,
    gold INTEGER NOT NULL,
    player_level INTEGER NOT NULL,
    xp INTEGER NOT NULL,
    board_json TEXT NOT NULL,
    augments_json TEXT NOT NULL,
    outcome TEXT CHECK (outcome IN ('WIN', 'LOSS', 'DRAW')),
    post_health INTEGER,
    UNIQUE (run_id, round_number)
);

CREATE INDEX idx_analytics_match_started_mode ON analytics_match(started_at DESC, mode);
CREATE INDEX idx_analytics_match_status ON analytics_match(status);
CREATE INDEX idx_analytics_run_client ON analytics_player_run(analytics_client_id);
CREATE INDEX idx_analytics_run_abandoned ON analytics_player_run(abandoned_at);
CREATE INDEX idx_analytics_run_match ON analytics_player_run(match_id);
CREATE INDEX idx_analytics_round_run ON analytics_player_round(run_id, round_number);
CREATE INDEX idx_analytics_round_outcome ON analytics_player_round(outcome);
