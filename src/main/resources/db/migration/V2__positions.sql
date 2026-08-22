CREATE TABLE positions (
    id      BIGSERIAL PRIMARY KEY,
    game_id BIGINT      NOT NULL REFERENCES games (id) ON DELETE CASCADE,
    ply     INT         NOT NULL,
    fen     TEXT        NOT NULL,
    san     VARCHAR(10) NOT NULL,
    uci     VARCHAR(6)  NOT NULL
);

CREATE UNIQUE INDEX ux_positions_game_ply ON positions (game_id, ply);
CREATE INDEX ix_positions_fen ON positions (fen);