ALTER TABLE analytics_player_round ADD COLUMN opponent_type TEXT
    CHECK (opponent_type IN ('BOT', 'HUMAN', 'GHOST', 'UNKNOWN'));

UPDATE analytics_player_round SET opponent_type = 'UNKNOWN' WHERE opponent_type IS NULL;

CREATE INDEX idx_analytics_round_opponent_outcome ON analytics_player_round(opponent_type, outcome, round_number);
