-- OpportunityGuard AI database schema (MySQL 8+)
CREATE DATABASE IF NOT EXISTS opportunityguard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE opportunityguard;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(190) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- PBKDF2-HMAC-SHA256 hash, never plain text
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS opportunities (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    title             VARCHAR(255) NOT NULL,
    organization      VARCHAR(255) NULL,
    type              VARCHAR(50)  NOT NULL DEFAULT 'Other',
    description       TEXT         NULL,
    deadline          DATE         NULL,
    registration_link VARCHAR(1000) NULL,
    eligibility       TEXT         NULL,
    trust_verdict     VARCHAR(30)  NOT NULL DEFAULT 'Unverified',
    trust_confidence  INT          NULL,
    registered        BOOLEAN      NOT NULL DEFAULT FALSE,
    idea_submitted    BOOLEAN      NOT NULL DEFAULT FALSE,
    ppt_submitted     BOOLEAN      NOT NULL DEFAULT FALSE,
    demo_submitted    BOOLEAN      NOT NULL DEFAULT FALSE,
    final_submitted   BOOLEAN      NOT NULL DEFAULT FALSE,
    status            VARCHAR(30)  NOT NULL DEFAULT 'Not Registered',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_opp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_opp_user_deadline (user_id, deadline)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    opportunity_id BIGINT       NULL,
    title          VARCHAR(255) NOT NULL,
    message        TEXT         NOT NULL,
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_opp  FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id, is_read, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS email_records (
    id               BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    gmail_message_id VARCHAR(64) NOT NULL,
    sender           VARCHAR(500) NULL,
    subject          VARCHAR(500) NULL,
    email_content    MEDIUMTEXT  NULL,
    analyzed         BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_email_user_msg (user_id, gmail_message_id)
) ENGINE=InnoDB;

-- Supporting table: per-user Google OAuth refresh token (the user's Gmail password is never seen).
CREATE TABLE IF NOT EXISTS gmail_tokens (
    user_id       BIGINT    NOT NULL PRIMARY KEY,
    refresh_token TEXT      NOT NULL,
    connected_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_sync_at  TIMESTAMP NULL,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
