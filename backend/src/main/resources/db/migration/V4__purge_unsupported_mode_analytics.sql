DELETE FROM analytics_player_round
WHERE run_id IN (
    SELECT r.id
    FROM analytics_player_run r
    JOIN analytics_match m ON m.id = r.match_id
    WHERE LOWER(REPLACE(m.mode, '_', '')) NOT IN ('onepiece', 'pokemon')
);

DELETE FROM analytics_player_run
WHERE match_id IN (
    SELECT id
    FROM analytics_match
    WHERE LOWER(REPLACE(mode, '_', '')) NOT IN ('onepiece', 'pokemon')
);

DELETE FROM analytics_match
WHERE LOWER(REPLACE(mode, '_', '')) NOT IN ('onepiece', 'pokemon');
