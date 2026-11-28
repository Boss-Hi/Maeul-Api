-- 사진 미션 (ADR 0017). 앵커 장소의 기준 사진을 퀴즈로 보여주고, 같은 곳을 찍어 올리면 성공해 가챠권 1장을 준다.
-- 사용자가 올린 사진은 저장하지 않는다. 시도 기록에는 판정 결과와 사진 해시만 남긴다.

-- 사진 미션 장소: 앵커 일정에 title 이 keyword 를 포함하는 항목이 있으면 그 일정에 사진 미션을 붙인다
CREATE TABLE IF NOT EXISTS `photo_mission_places`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `keyword`    VARCHAR(100) NOT NULL COMMENT '일정 항목 제목에 들어 있어야 하는 장소 키워드',
    `name`       VARCHAR(100) NOT NULL COMMENT '사용자에게 보여 주는 장소 이름',
    `latitude`   DOUBLE       NOT NULL,
    `longitude`  DOUBLE       NOT NULL,
    `radius_m`   INT          NOT NULL COMMENT '촬영 허용 반경(미터)',
    `enabled`    BIT(1)       NOT NULL,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_photo_mission_places_keyword` (`keyword`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 기준 사진: 운영자가 이미지 URL 만 등록한다 (업로드 저장소 없음)
CREATE TABLE IF NOT EXISTS `photo_mission_reference_photos`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `place_id`   BIGINT       NOT NULL,
    `image_url`  VARCHAR(500) NOT NULL,
    `enabled`    BIT(1)       NOT NULL,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    KEY `idx_photo_mission_reference_photos_place` (`place_id`),
    CONSTRAINT `fk_photo_mission_reference_photos_place` FOREIGN KEY (`place_id`) REFERENCES `photo_mission_places` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 일정에 붙은 사진 미션. 기준 사진은 붙을 때 정해져 바뀌지 않는다
CREATE TABLE IF NOT EXISTS `photo_missions`
(
    `id`                   BIGINT      NOT NULL AUTO_INCREMENT,
    `itinerary_id`         BIGINT      NOT NULL,
    `user_id`              BIGINT      NOT NULL,
    `place_id`             BIGINT      NOT NULL,
    `reference_photo_id`   BIGINT      NOT NULL,
    `status`               VARCHAR(16) NOT NULL COMMENT 'READY, COMPLETE',
    `region_code`          VARCHAR(16) NULL COMMENT '일정 주축제의 시도 코드',
    `subdivision_code`     VARCHAR(16) NULL COMMENT '일정 주축제의 시군구 코드',
    `start_date`           DATE        NOT NULL,
    `end_date`             DATE        NOT NULL,
    `completed_at`         DATETIME(6) NULL,
    `rewarded`             BIT(1)      NOT NULL COMMENT '가챠권을 발급했는가 (같은 시군구에 기간이 겹치는 일정은 한 번만)',
    `created_at`           DATETIME(6) NOT NULL,
    `updated_at`           DATETIME(6) NOT NULL,
    `deleted_at`           DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_photo_missions_itinerary_place` (`itinerary_id`, `place_id`),
    KEY `idx_photo_missions_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 시도 기록: 사진은 저장하지 않고 해시와 판정만 남긴다. AI 사유는 서버 기록용이며 사용자에게 보이지 않는다
CREATE TABLE IF NOT EXISTS `photo_mission_attempts`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `photo_mission_id` BIGINT       NOT NULL,
    `user_id`          BIGINT       NOT NULL,
    `attempted_on`     DATE         NOT NULL COMMENT '시도한 날(서비스 시간대)',
    `success`          BIT(1)       NOT NULL,
    `latitude`         DOUBLE       NOT NULL,
    `longitude`        DOUBLE       NOT NULL,
    `distance_m`       DOUBLE       NOT NULL,
    `image_hash`       CHAR(64)     NOT NULL COMMENT '올린 사진의 SHA-256',
    `result`           VARCHAR(32)  NOT NULL COMMENT 'SUCCESS, NOT_SAME, LOW_CONFIDENCE, EXIF_STALE',
    `ai_provider`      VARCHAR(32)  NULL,
    `ai_same`          BIT(1)       NULL,
    `ai_confidence`    DOUBLE       NULL,
    `ai_reason`        VARCHAR(1000) NULL,
    `created_at`       DATETIME(6)  NOT NULL,
    `updated_at`       DATETIME(6)  NOT NULL,
    `deleted_at`       DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    KEY `idx_photo_mission_attempts_mission_day` (`photo_mission_id`, `attempted_on`),
    KEY `idx_photo_mission_attempts_user_hash` (`user_id`, `image_hash`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 정조효공원·융건릉. 좌표는 주축제 '세계유산 조선왕릉축전'(효행로481번길 21)의 좌표다. 기준 사진을 등록하기 전까지 사진 미션은 붙지 않는다
INSERT INTO `photo_mission_places` (`keyword`, `name`, `latitude`, `longitude`, `radius_m`, `enabled`, `created_at`, `updated_at`)
VALUES ('융건릉', '정조효공원·융건릉', 37.2081453806, 126.9892183546, 150, b'1', NOW(6), NOW(6));

-- 사진 미션 성공 가챠권 발급 설정 (관리자가 켜고 끄고 유효기간을 바꾼다)
INSERT INTO `gacha_ticket_rules` (`event_type`, `enabled`, `validity_days`, `created_at`, `updated_at`)
VALUES ('PHOTO_MISSION', b'1', 30, NOW(6), NOW(6));
