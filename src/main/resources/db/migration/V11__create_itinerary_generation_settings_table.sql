-- 일정 생성에 적용한 설정. 일정 하나에 하나이며, 이 테이블이 생기기 전에 만든 일정은 행이 없다.
CREATE TABLE itinerary_generation_settings
(
    `id`             BIGINT      NOT NULL AUTO_INCREMENT,
    `itinerary_id`   BIGINT      NOT NULL,
    `pace`           VARCHAR(16) NOT NULL COMMENT '적용한 페이스 (RELAXED, MODERATE, PACKED)',
    `day_start_time` TIME        NOT NULL COMMENT '적용한 하루 시작 시간',
    `day_end_time`   TIME        NOT NULL COMMENT '적용한 하루 마감 시간',
    `created_at`     DATETIME(6) NOT NULL,
    `updated_at`     DATETIME(6) NOT NULL,
    `deleted_at`     DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_itinerary_generation_settings_itinerary` (`itinerary_id`),
    CONSTRAINT `fk_itinerary_generation_settings_itinerary` FOREIGN KEY (`itinerary_id`) REFERENCES `itineraries` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
