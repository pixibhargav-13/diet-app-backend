-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';

-- Sessions table
CREATE TABLE sessions (
    id                  BIGSERIAL PRIMARY KEY,
    client_id           BIGINT        NOT NULL REFERENCES users(id),
    type                VARCHAR(20)   NOT NULL,
    session_date        DATE          NOT NULL,
    session_time        VARCHAR(10)   NOT NULL,
    duration_minutes    INT           NOT NULL DEFAULT 60,
    repeat_type         VARCHAR(20)   NOT NULL DEFAULT 'none',
    meet_link           VARCHAR(500),
    status              VARCHAR(20)   NOT NULL DEFAULT 'scheduled',
    agreement_accepted  BOOLEAN       NOT NULL DEFAULT FALSE,
    notes               TEXT,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Reschedule history
CREATE TABLE reschedule_history (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT        NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    from_date       DATE          NOT NULL,
    from_time       VARCHAR(10)   NOT NULL,
    to_date         DATE          NOT NULL,
    to_time         VARCHAR(10)   NOT NULL,
    rescheduled_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Session packages
CREATE TABLE session_packages (
    id              BIGSERIAL PRIMARY KEY,
    client_id       BIGINT          NOT NULL REFERENCES users(id),
    package_name    VARCHAR(200)    NOT NULL,
    session_count   INT             NOT NULL,
    sessions_used   INT             NOT NULL DEFAULT 0,
    price           DECIMAL(10, 2)  NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Session policies (null client_id = global default)
CREATE TABLE session_policies (
    id                          BIGSERIAL PRIMARY KEY,
    client_id                   BIGINT REFERENCES users(id),
    cancellation_notice_hours   INT NOT NULL DEFAULT 24,
    max_reschedule_per_month    INT NOT NULL DEFAULT 2,
    created_at                  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at                  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_client_policy UNIQUE (client_id)
);

-- Seed global policy (client_id = NULL means global)
INSERT INTO session_policies (client_id, cancellation_notice_hours, max_reschedule_per_month)
VALUES (NULL, 24, 2);

-- Index for common queries
CREATE INDEX idx_sessions_client_id ON sessions(client_id);
CREATE INDEX idx_sessions_status    ON sessions(status);
CREATE INDEX idx_reschedule_session ON reschedule_history(session_id);
CREATE INDEX idx_packages_client_id ON session_packages(client_id);
