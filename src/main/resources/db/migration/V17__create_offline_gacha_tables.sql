-- 오프라인 가챠 (ADR 0016). 가챠권 → 매장 사장님 스캔 → 장비 → 상품 QR 스캔.
-- 결과는 실제 장비가 정하므로 확률 테이블은 없다. 재고는 발급한 상품 QR 개수다.

-- 매장: 운영자가 등록하고 시군구를 가진다 (region_subdivisions 의 region_code 와 code 를 각각 저장)
CREATE TABLE IF NOT EXISTS `stores`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(100) NOT NULL,
    `region_code`      VARCHAR(16)  NOT NULL COMMENT '시도 코드 (region_subdivisions.region_code)',
    `subdivision_code` VARCHAR(16)  NOT NULL COMMENT '시군구 코드 (region_subdivisions.code)',
    `created_at`       DATETIME(6)  NOT NULL,
    `updated_at`       DATETIME(6)  NOT NULL,
    `deleted_at`       DATETIME(6)  NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사장님: 매장에 연결된 사용자. 한 사용자는 매장 하나에만 속한다
CREATE TABLE IF NOT EXISTS `store_owners`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `store_id`   BIGINT      NOT NULL,
    `user_id`    BIGINT      NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_store_owners_user` (`user_id`),
    KEY `idx_store_owners_store` (`store_id`),
    CONSTRAINT `fk_store_owners_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 가챠권 발급 설정: 이벤트 종류별로 켜고 끄고 유효기간을 정한다 (바꾼 값은 그 뒤 발급분부터)
CREATE TABLE IF NOT EXISTS `gacha_ticket_rules`
(
    `event_type`    VARCHAR(32) NOT NULL COMMENT 'ITINERARY_COMPLETION',
    `enabled`       BIT(1)      NOT NULL,
    `validity_days` INT         NOT NULL COMMENT '발급일로부터 유효 일수',
    `created_at`    DATETIME(6) NOT NULL,
    `updated_at`    DATETIME(6) NOT NULL,
    `deleted_at`    DATETIME(6) NULL,
    PRIMARY KEY (`event_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO `gacha_ticket_rules` (`event_type`, `enabled`, `validity_days`, `created_at`, `updated_at`)
VALUES ('ITINERARY_COMPLETION', b'1', 30, NOW(6), NOW(6));

-- 가챠권: 코드는 QR 에 담기는 1회용 값. 사용되면 used_* 가 채워지고, 운영자가 취소하면 비워진다
CREATE TABLE IF NOT EXISTS `gacha_tickets`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL,
    `code`         VARCHAR(40)  NOT NULL,
    `event_type`   VARCHAR(32)  NOT NULL,
    `ref_key`      VARCHAR(100) NOT NULL COMMENT '발급을 일으킨 일 (예: itinerary:12). 같은 일로 두 번 발급하지 않는다',
    `expires_at`   DATETIME(6)  NOT NULL,
    `used_at`      DATETIME(6)  NULL,
    `used_store_id`   BIGINT    NULL,
    `used_by_user_id` BIGINT    NULL COMMENT '스캔한 사장님',
    `created_at`   DATETIME(6)  NOT NULL,
    `updated_at`   DATETIME(6)  NOT NULL,
    `deleted_at`   DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_gacha_tickets_code` (`code`),
    UNIQUE KEY `uk_gacha_tickets_ref` (`user_id`, `event_type`, `ref_key`),
    KEY `idx_gacha_tickets_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 오프라인 상품 종류 (서버 가챠의 prizes 와 별개)
CREATE TABLE IF NOT EXISTS `offline_prize_kinds`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 상품 QR: 운영자가 미리 발급한 고유 1회용 코드. ticket_id 는 가챠권 한 장에 하나만 연결된다
CREATE TABLE IF NOT EXISTS `prize_qrs`
(
    `id`                   BIGINT      NOT NULL AUTO_INCREMENT,
    `kind_id`              BIGINT      NOT NULL,
    `code`                 VARCHAR(40) NOT NULL,
    `delivered_at`         DATETIME(6) NULL,
    `delivered_store_id`   BIGINT      NULL,
    `delivered_by_user_id` BIGINT      NULL COMMENT '스캔한 사장님',
    `ticket_id`            BIGINT      NULL,
    `created_at`           DATETIME(6) NOT NULL,
    `updated_at`           DATETIME(6) NOT NULL,
    `deleted_at`           DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prize_qrs_code` (`code`),
    UNIQUE KEY `uk_prize_qrs_ticket` (`ticket_id`),
    KEY `idx_prize_qrs_kind` (`kind_id`),
    CONSTRAINT `fk_prize_qrs_kind` FOREIGN KEY (`kind_id`) REFERENCES `offline_prize_kinds` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 사용 이력과 취소 기록 (매장 이력 조회, 누가 언제 왜 취소했는지)
CREATE TABLE IF NOT EXISTS `offline_gacha_logs`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `action`        VARCHAR(32)  NOT NULL COMMENT 'TICKET_USED, PRIZE_DELIVERED, TICKET_USE_CANCELLED, PRIZE_DELIVERY_CANCELLED',
    `ticket_id`     BIGINT       NULL,
    `prize_qr_id`   BIGINT       NULL,
    `store_id`      BIGINT       NULL,
    `actor_user_id` BIGINT       NOT NULL COMMENT '스캔한 사장님 또는 취소한 운영자',
    `reason`        VARCHAR(500) NULL,
    `created_at`    DATETIME(6)  NOT NULL,
    `updated_at`    DATETIME(6)  NOT NULL,
    `deleted_at`    DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    KEY `idx_offline_gacha_logs_store` (`store_id`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
