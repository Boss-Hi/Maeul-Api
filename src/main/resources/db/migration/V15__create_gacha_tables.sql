-- 가챠 (ADR 0014). 결과 목록, 가중치, 보상 값, 상품 재고, 아이템 종류는 관리자 화면에서 고친다.

-- 상품: 운영자가 실제로 지급하는 보상. 재고가 0이면 가챠에서 빠진다.
CREATE TABLE IF NOT EXISTS `prizes`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL,
    `stock`      INT          NOT NULL COMMENT '남은 재고',
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 아이템 종류: 펫을 꾸미는 외형
CREATE TABLE IF NOT EXISTS `items`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL,
    `image_url`  VARCHAR(500) NULL,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 가챠 결과 목록. 확률 = weight / (그 가챠에서 뽑을 수 있는 결과의 weight 합)
CREATE TABLE IF NOT EXISTS `gacha_outcomes`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `gacha_type`  VARCHAR(32)  NOT NULL COMMENT 'GRADUATION, ITINERARY_COMPLETION, MISSION',
    `reward_type` VARCHAR(16)  NOT NULL COMMENT 'PRIZE, ITEM, POINT, FAT_EXP, NONE(꽝)',
    `label`       VARCHAR(100) NOT NULL COMMENT '사용자에게 보여 주는 이름',
    `weight`      INT          NOT NULL COMMENT '가중치. 0이면 뽑지 않음',
    `amount`      BIGINT       NULL COMMENT 'POINT: 포인트, FAT_EXP: 펫 경험치',
    `prize_id`    BIGINT       NULL COMMENT 'PRIZE: 상품',
    `item_id`     BIGINT       NULL COMMENT 'ITEM: 아이템',
    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    KEY `idx_gacha_outcomes_type` (`gacha_type`),
    CONSTRAINT `fk_gacha_outcomes_prize` FOREIGN KEY (`prize_id`) REFERENCES `prizes` (`id`),
    CONSTRAINT `fk_gacha_outcomes_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 가챠 결과: 뽑는 순간 정해진 값을 그대로 남긴다. 같은 일(ref_key)로는 한 번만 뽑는다.
CREATE TABLE IF NOT EXISTS `gacha_results`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `gacha_type`  VARCHAR(32)  NOT NULL,
    `ref_key`     VARCHAR(64)  NOT NULL COMMENT '가챠를 일으킨 일 (예: fat:3, mission:12, itinerary:5)',
    `outcome_id`  BIGINT       NOT NULL,
    `reward_type` VARCHAR(16)  NOT NULL,
    `label`       VARCHAR(100) NOT NULL,
    `amount`      BIGINT       NULL,
    `prize_id`    BIGINT       NULL,
    `item_id`     BIGINT       NULL,
    `seen_at`     DATETIME(6)  NULL COMMENT '사용자가 확인한 시각. NULL 이면 미확인',
    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_gacha_results_user_type_ref` (`user_id`, `gacha_type`, `ref_key`),
    KEY `idx_gacha_results_user_seen` (`user_id`, `seen_at`),
    CONSTRAINT `fk_gacha_results_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 교환권: 가챠에서 상품이 나오면 발급한다. 지급 완료는 운영자가 정한다.
CREATE TABLE IF NOT EXISTS `prize_vouchers`
(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT      NOT NULL,
    `prize_id`        BIGINT      NOT NULL,
    `gacha_result_id` BIGINT      NOT NULL,
    `status`          VARCHAR(16) NOT NULL COMMENT 'ISSUED(발급됨), FULFILLED(지급 완료)',
    `fulfilled_at`    DATETIME(6) NULL,
    `created_at`      DATETIME(6) NOT NULL,
    `updated_at`      DATETIME(6) NOT NULL,
    `deleted_at`      DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prize_vouchers_gacha_result` (`gacha_result_id`),
    KEY `idx_prize_vouchers_user` (`user_id`),
    CONSTRAINT `fk_prize_vouchers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_prize_vouchers_prize` FOREIGN KEY (`prize_id`) REFERENCES `prizes` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용자가 가진 아이템. 같은 아이템이 또 나오면 수량이 는다.
CREATE TABLE IF NOT EXISTS `user_items`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `item_id`    BIGINT      NOT NULL,
    `quantity`   INT         NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_items_user_item` (`user_id`, `item_id`),
    CONSTRAINT `fk_user_items_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 미션 가챠로 펫이 얻은 경험치 원장. 같은 미션은 한 번만 준다. 위치 기록을 지워도 회수하지 않는다.
CREATE TABLE IF NOT EXISTS `fat_mission_exps`
(
    `id`                          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`                     BIGINT      NOT NULL,
    `fat_id`                      BIGINT      NOT NULL,
    `itinerary_mission_id`        BIGINT      NOT NULL COMMENT '완료한 미션 (itinerary_mission_mappings.id)',
    `gacha_result_id`             BIGINT      NOT NULL,
    `exp`                         BIGINT      NOT NULL,
    `created_at`                  DATETIME(6) NOT NULL,
    `updated_at`                  DATETIME(6) NOT NULL,
    `deleted_at`                  DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fat_mission_exps_user_mission` (`user_id`, `itinerary_mission_id`),
    KEY `idx_fat_mission_exps_fat` (`fat_id`),
    CONSTRAINT `fk_fat_mission_exps_fat` FOREIGN KEY (`fat_id`) REFERENCES `fats` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 일정 완료: 같은 주축제 시군구에 기간이 겹치는 일정들을 묶어 한 번만 완료한다. 묶음의 일정마다 1행
CREATE TABLE IF NOT EXISTS `itinerary_completions`
(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT      NOT NULL,
    `itinerary_id`    BIGINT      NOT NULL COMMENT '완료된 일정 (itineraries.id)',
    `gacha_result_id` BIGINT      NOT NULL,
    `created_at`      DATETIME(6) NOT NULL,
    `updated_at`      DATETIME(6) NOT NULL,
    `deleted_at`      DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_itinerary_completions_itinerary` (`itinerary_id`),
    CONSTRAINT `fk_itinerary_completions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 테스트용 기본값: 결과마다 같은 가중치(1/3)
INSERT INTO `prizes` (`id`, `name`, `stock`, `created_at`, `updated_at`)
VALUES (1, '테스트 상품', 10, NOW(6), NOW(6));

INSERT INTO `items` (`id`, `name`, `created_at`, `updated_at`)
VALUES (1, '테스트 아이템', NOW(6), NOW(6));

INSERT INTO `gacha_outcomes` (`gacha_type`, `reward_type`, `label`, `weight`, `amount`, `prize_id`, `item_id`, `created_at`, `updated_at`)
VALUES ('GRADUATION', 'PRIZE', '테스트 상품', 1, NULL, 1, NULL, NOW(6), NOW(6)),
       ('GRADUATION', 'ITEM', '테스트 아이템', 1, NULL, NULL, 1, NOW(6), NOW(6)),
       ('GRADUATION', 'POINT', '500 포인트', 1, 500, NULL, NULL, NOW(6), NOW(6)),
       ('ITINERARY_COMPLETION', 'ITEM', '테스트 아이템', 1, NULL, NULL, 1, NOW(6), NOW(6)),
       ('ITINERARY_COMPLETION', 'POINT', '100 포인트', 1, 100, NULL, NULL, NOW(6), NOW(6)),
       ('ITINERARY_COMPLETION', 'NONE', '꽝', 1, NULL, NULL, NULL, NOW(6), NOW(6)),
       ('MISSION', 'FAT_EXP', '펫 경험치 12시간', 1, 720, NULL, NULL, NOW(6), NOW(6)),
       ('MISSION', 'FAT_EXP', '펫 경험치 6시간', 1, 360, NULL, NULL, NOW(6), NOW(6)),
       ('MISSION', 'FAT_EXP', '펫 경험치 3시간', 1, 180, NULL, NULL, NOW(6), NOW(6));
