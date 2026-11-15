-- 펫이 자라는 일정 기간. 일정 모듈이 보낸 이벤트로 채우는 복사본이다 (ADR 0013).
-- 시군구는 일정 주축제의 시도 코드와 시군구 코드이며, 기간은 시작일 0시부터 종료일 24시까지다.
CREATE TABLE IF NOT EXISTS `fat_itinerary_periods`
(
    `id`               BIGINT      NOT NULL AUTO_INCREMENT,
    `itinerary_id`     BIGINT      NOT NULL COMMENT '일정 ID (itineraries.id)',
    `user_id`          BIGINT      NOT NULL,
    `region_code`      VARCHAR(16) NOT NULL COMMENT '시도 코드 (region_subdivisions.region_code)',
    `subdivision_code` VARCHAR(16) NOT NULL COMMENT '시군구 코드 (region_subdivisions.code)',
    `start_date`       DATE        NOT NULL,
    `end_date`         DATE        NOT NULL,
    `created_at`       DATETIME(6) NOT NULL,
    `updated_at`       DATETIME(6) NOT NULL,
    `deleted_at`       DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fat_itinerary_periods_itinerary` (`itinerary_id`),
    KEY `idx_fat_itinerary_periods_user_region` (`user_id`, `region_code`, `subdivision_code`),
    CONSTRAINT `fk_fat_itinerary_periods_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 방문일 1회에 주는 경험치
ALTER TABLE `fat_settings`
    ADD COLUMN `visit_day_exp` INT NOT NULL DEFAULT 60 COMMENT '방문일 하루에 주는 경험치' AFTER `max_active_fats`;
