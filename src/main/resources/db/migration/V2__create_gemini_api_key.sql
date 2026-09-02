CREATE TABLE IF NOT EXISTS `gemini_api_keys`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `api_key`      VARCHAR(255) NOT NULL UNIQUE,
    `active`       TINYINT(1)   NOT NULL DEFAULT 1,
    `last_used_at` DATETIME(6)  NULL,
    `created_at`   DATETIME(6)  NOT NULL,
    `updated_at`   DATETIME(6)  NOT NULL,
    `deleted_at`   DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_gemini_api_keys_active_last_used` (`active`, `last_used_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
