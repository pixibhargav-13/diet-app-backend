CREATE TABLE IF NOT EXISTS users
(
    id                          BIGSERIAL                NOT NULL PRIMARY KEY,
    first_name                  VARCHAR(100)             NOT NULL,
    last_name                   VARCHAR(100)             NOT NULL,
    email                       VARCHAR(255)             NOT NULL UNIQUE,
    password                    VARCHAR(255)             NOT NULL,
    enabled                     BOOLEAN                  NOT NULL DEFAULT TRUE,
    password_reset_token        VARCHAR(255),
    password_reset_token_expiry TIMESTAMP WITH TIME ZONE,
    created_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
