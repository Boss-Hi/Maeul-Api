-- 주인 레벨표. 병아리 레벨표와 같은 구조이며 건국(교환) 레벨은 없다. 관리자 화면에서 조정한다.
CREATE TABLE IF NOT EXISTS `master_levels`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `level`        INT         NOT NULL,
    `required_exp` BIGINT      NOT NULL COMMENT '이 레벨에서 다음 레벨까지 필요한 경험치. 최대 레벨에서는 쓰지 않음',
    `stage_name`   VARCHAR(32) NULL COMMENT '단계 이름 (이 레벨부터 다음 단계 이름 전까지 표시)',
    `created_at`   DATETIME(6) NOT NULL,
    `updated_at`   DATETIME(6) NOT NULL,
    `deleted_at`   DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_master_levels_level` (`level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기본 주인 레벨표: Lv.1 ~ Lv.100, 레벨마다 1,008 (병아리 1마리 건국 10,080 = 주인 10레벨)
INSERT INTO `master_levels` (`level`, `required_exp`, `stage_name`, `created_at`, `updated_at`)
WITH RECURSIVE seq (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 100)
SELECT n, IF(n < 100, 1008, 0), NULL, NOW(6), NOW(6)
FROM seq;

-- 주인 경험치 = 모든 병아리 경험치 합 x 주인 비율
ALTER TABLE `fat_settings`
    ADD COLUMN `master_exp_ratio` DECIMAL(6, 3) NOT NULL DEFAULT 1.000 COMMENT '주인 경험치 비율 (병아리 경험치 합에 곱함)' AFTER `visit_day_exp`;
