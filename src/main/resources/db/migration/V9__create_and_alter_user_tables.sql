CREATE TABLE IF NOT EXISTS `auth_types`
(
    `code`        VARCHAR(32)  NOT NULL,
    `name`        VARCHAR(256) NOT NULL UNIQUE,
    `description` VARCHAR(256) NOT NULL,
    PRIMARY KEY (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

ALTER TABLE users
    ADD COLUMN auth_type VARCHAR(32) NOT NULL COMMENT '로그인 유형';
ALTER TABLE users
    ADD COLUMN auth_id VARCHAR(256) NOT NULL COMMENT '로그인 id';
ALTER TABLE users
    ADD FOREIGN KEY (auth_type) REFERENCES auth_types (code) ON DELETE SET NULL ON UPDATE CASCADE;