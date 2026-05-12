CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE assets (
    id           BIGSERIAL    PRIMARY KEY,
    symbol       VARCHAR(20)  NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    category     VARCHAR(50)  NOT NULL,
    stooq_symbol VARCHAR(20)  NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE price_points (
    id        BIGSERIAL     PRIMARY KEY,
    asset_id  BIGINT        NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    timestamp DATE          NOT NULL,
    open      NUMERIC(18,6),
    high      NUMERIC(18,6),
    low       NUMERIC(18,6),
    close     NUMERIC(18,6) NOT NULL,
    volume    BIGINT,
    UNIQUE (asset_id, timestamp)
);

CREATE INDEX idx_price_points_asset_ts ON price_points (asset_id, timestamp);

CREATE TABLE dashboards (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE widgets (
    id            BIGSERIAL   PRIMARY KEY,
    dashboard_id  BIGINT      NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
    asset_id      BIGINT      NOT NULL REFERENCES assets(id),
    chart_type    VARCHAR(20) NOT NULL,
    time_range    VARCHAR(20) NOT NULL,
    color         VARCHAR(7)  NOT NULL DEFAULT '#3b82f6',
    title         VARCHAR(100),
    display_order INT         NOT NULL DEFAULT 0
);
