-- 펫 레벨표. 레벨마다 다음 레벨까지 필요한 경험치(1분 = 1)와 단계 이름. 관리자 화면에서 조정한다.
-- 가장 높은 레벨이 최대 레벨이고, founding 이 켜진 가장 낮은 레벨이 건국(상품 교환 가능) 레벨이다.
CREATE TABLE IF NOT EXISTS `fat_levels`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `level`        INT         NOT NULL,
    `required_exp` BIGINT      NOT NULL COMMENT '이 레벨에서 다음 레벨까지 필요한 경험치. 최대 레벨에서는 쓰지 않음',
    `stage_name`   VARCHAR(32) NULL COMMENT '단계 이름 (이 레벨부터 다음 단계 이름 전까지 표시)',
    `founding`     BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '건국(상품 교환 가능) 레벨 여부',
    `created_at`   DATETIME(6) NOT NULL,
    `updated_at`   DATETIME(6) NOT NULL,
    `deleted_at`   DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fat_levels_level` (`level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기본 레벨표: Lv.1 ~ Lv.100, 레벨마다 126분. Lv.81 건국 (80 x 126 = 10,080분 = 숙박 포함 7일)
INSERT INTO `fat_levels` (`level`, `required_exp`, `stage_name`, `founding`, `created_at`, `updated_at`)
WITH RECURSIVE seq (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 100)
SELECT n,
       IF(n < 100, 126, 0),
       IF(n = 81, '건국', NULL),
       n = 81,
       NOW(6),
       NOW(6)
FROM seq;

-- 펫. 시군구는 region_subdivisions 의 한 행이며, 사용자마다 시군구 하나에 키우는 중인(졸업하지 않은) 펫은 1마리
CREATE TABLE IF NOT EXISTS `fats`
(
    `id`                           BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`                      BIGINT      NOT NULL,
    `region_subdivision_id`        BIGINT      NOT NULL COMMENT '시군구 (region_subdivisions.id)',
    `adopted_at`                   DATETIME(6) NOT NULL COMMENT '분양 시각 (timeline.zone 기준). 경험치는 이 시각 이후 점령 시간',
    `adoption_day_seconds`         BIGINT      NOT NULL DEFAULT 0 COMMENT '분양 당일 분양 시각 이후 점령 시간. 그날 위치 기록이 바뀔 때 다시 계산',
    `graduated_at`                 DATETIME(6) NULL COMMENT '졸업(상품 교환) 시각. 되돌리지 않음',
    `graduated_level`              INT         NULL COMMENT '졸업 때 레벨',
    `graduated_exp`                BIGINT      NULL COMMENT '졸업 때 경험치',
    `active_region_subdivision_id` BIGINT AS (IF(`graduated_at` IS NULL, `region_subdivision_id`, NULL)) STORED COMMENT '키우는 중인 펫 중복 방지용',
    `created_at`                   DATETIME(6) NOT NULL,
    `updated_at`                   DATETIME(6) NOT NULL,
    `deleted_at`                   DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fats_user_active_region_subdivision` (`user_id`, `active_region_subdivision_id`),
    KEY `idx_fats_user_region_subdivision` (`user_id`, `region_subdivision_id`),
    CONSTRAINT `fk_fats_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fats_region_subdivision` FOREIGN KEY (`region_subdivision_id`) REFERENCES `region_subdivisions` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 펫 설정. 1행만 쓰며 관리자 화면에서 고친다.
CREATE TABLE IF NOT EXISTS `fat_settings`
(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `max_active_fats` INT         NOT NULL COMMENT '사용자가 동시에 키울 수 있는 펫 수. 줄여도 이미 키우는 펫은 그대로',
    `created_at`      DATETIME(6) NOT NULL,
    `updated_at`      DATETIME(6) NOT NULL,
    `deleted_at`      DATETIME(6) NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO `fat_settings` (`id`, `max_active_fats`, `created_at`, `updated_at`)
VALUES (1, 3, NOW(6), NOW(6));
