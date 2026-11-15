-- 병아리 설정. 1행만 쓰며 관리자 화면에서 고친다.
CREATE TABLE IF NOT EXISTS `fat_settings`
(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `max_active_fats` INT         NOT NULL COMMENT '사용자가 동시에 키울 수 있는 병아리 수. 줄여도 이미 키우는 병아리는 그대로',
    `created_at`      DATETIME(6) NOT NULL,
    `updated_at`      DATETIME(6) NOT NULL,
    `deleted_at`      DATETIME(6) NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO `fat_settings` (`id`, `max_active_fats`, `created_at`, `updated_at`)
VALUES (1, 3, NOW(6), NOW(6));
