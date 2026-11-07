-- 좌표 격자(약 100m) -> 시군구 캐시 (카카오 역지오코딩 결과)
CREATE TABLE IF NOT EXISTS `territory_region_cells`
(
    `cell_key`         VARCHAR(32) NOT NULL COMMENT '격자 키 (위도:경도, 소수점 3자리)',
    `region_id`        VARCHAR(10) NULL COMMENT '시군구 코드 5자리 (법정동 코드 앞 5자리). NULL 이면 지역 밖(바다 등)',
    `region_name`      VARCHAR(64) NULL,
    `parent_region_id` VARCHAR(10) NULL COMMENT '시도 코드 2자리',
    `parent_name`      VARCHAR(64) NULL,
    `created_at`       DATETIME(6) NOT NULL,
    `updated_at`       DATETIME(6) NOT NULL,
    `deleted_at`       DATETIME(6) NULL,
    PRIMARY KEY (`cell_key`),
    KEY `idx_territory_region_cells_region` (`region_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자 x 시군구 x 날짜별 점령 시간 (위치 기록에서 다시 계산)
CREATE TABLE IF NOT EXISTS `territory_daily_stays`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT      NOT NULL,
    `region_id`    VARCHAR(10) NOT NULL,
    `stay_date`    DATE        NOT NULL,
    `stay_seconds` BIGINT      NOT NULL,
    `created_at`   DATETIME(6) NOT NULL,
    `updated_at`   DATETIME(6) NOT NULL,
    `deleted_at`   DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_territory_daily_stays` (`user_id`, `region_id`, `stay_date`),
    KEY `idx_territory_daily_stays_region_date` (`region_id`, `stay_date`),
    CONSTRAINT `fk_territory_daily_stays_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 포인트 원장 (잔액 = amount 합)
CREATE TABLE IF NOT EXISTS `point_transactions`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `amount`     BIGINT      NOT NULL,
    `reason`     VARCHAR(32) NOT NULL,
    `ref_key`    VARCHAR(64) NOT NULL COMMENT '같은 지급을 두 번 하지 않기 위한 키',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_point_transactions_user_ref` (`user_id`, `ref_key`),
    CONSTRAINT `fk_point_transactions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
