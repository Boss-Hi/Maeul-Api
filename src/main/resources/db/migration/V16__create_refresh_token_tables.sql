-- 앱·웹 로그인 유지 (ADR 0015)

-- 리프레시 토큰. 원문은 저장하지 않고 SHA-256 해시만 둔다.
-- 한 기기의 로그인 = family 하나. 쓸 때마다 같은 family 로 새 토큰을 발급하고, 이미 쓴 토큰이 다시 오면 family 전체를 폐기한다.
CREATE TABLE IF NOT EXISTS `refresh_tokens`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `family_id`  VARCHAR(36) NOT NULL COMMENT '기기 로그인 1회',
    `token_hash` CHAR(64)    NOT NULL COMMENT 'SHA-256 hex',
    `expires_at` DATETIME(6) NOT NULL,
    `used_at`    DATETIME(6) NULL COMMENT '새 토큰으로 바꾼 시각',
    `revoked_at` DATETIME(6) NULL COMMENT '로그아웃, 재사용 감지, 연결 끊기로 폐기한 시각',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_tokens_hash` (`token_hash`),
    KEY `idx_refresh_tokens_family` (`family_id`),
    KEY `idx_refresh_tokens_user` (`user_id`),
    CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 웹 OAuth 로그인 뒤 URL 로 넘기는 1회용 코드. 짧게 살고 한 번만 토큰으로 바꿀 수 있다.
CREATE TABLE IF NOT EXISTS `login_codes`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `code_hash`  CHAR(64)    NOT NULL COMMENT 'SHA-256 hex',
    `expires_at` DATETIME(6) NOT NULL,
    `used_at`    DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_codes_hash` (`code_hash`),
    CONSTRAINT `fk_login_codes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT IGNORE INTO `auth_types` (`code`, `name`, `description`)
VALUES ('APPLE', 'Apple', 'Apple 로그인 사용자입니다.');
