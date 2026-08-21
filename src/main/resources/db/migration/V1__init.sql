CREATE TABLE games (
    id           BIGSERIAL PRIMARY KEY,
    source       VARCHAR(20)  NOT NULL,
    external_id  VARCHAR(50)  NOT NULL,
    played_at    TIMESTAMPTZ,
    white        VARCHAR(100),
    black        VARCHAR(100),
    result       VARCHAR(10),
    eco          VARCHAR(10),
    time_control VARCHAR(30),
    pgn          TEXT         NOT NULL,
    imported_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_games_source_external_id ON games (source, external_id);
