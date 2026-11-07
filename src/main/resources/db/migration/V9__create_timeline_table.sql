CREATE TABLE IF NOT EXISTS `timeline_points`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `latitude`    DOUBLE      NOT NULL,
    `longitude`   DOUBLE      NOT NULL,
    `accuracy`    DOUBLE      NULL COMMENT '기기가 알려준 정확도 (m)',
    `recorded_at` DATETIME(6) NOT NULL COMMENT '기기에서 위치를 수집한 시각',
    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `deleted_at`  DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_timeline_points_user_recorded_at` (`user_id`, `recorded_at`),
    CONSTRAINT `fk_timeline_points_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
