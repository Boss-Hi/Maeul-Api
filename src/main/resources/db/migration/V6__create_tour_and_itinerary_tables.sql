-- Flyway migration: create schema tables based on current JPA entities
-- Generated: 2026-08-27

SET @@foreign_key_checks = 0;

CREATE TABLE IF NOT EXISTS `tour_content_types`
(
    `id`                         BIGINT      NOT NULL AUTO_INCREMENT,
    `content_type_id`            VARCHAR(16) NOT NULL UNIQUE,
    `content_type_id_multi_lang` VARCHAR(16),
    `name`                       VARCHAR(32) NOT NULL,
    `active`                     TINYINT(1)  NOT NULL DEFAULT 1,
    `created_at`                 DATETIME(6),
    `updated_at`                 DATETIME(6),
    `deleted_at`                 DATETIME(6),
    INDEX `idx_tour_content_types_content_type_id` (`content_type_id`),
    INDEX `idx_tour_content_types_content_type_id_multi_lang` (`content_type_id_multi_lang`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_categories`
(
    `id`                         BIGINT       NOT NULL AUTO_INCREMENT,
    `code`                       VARCHAR(16)  NOT NULL UNIQUE,
    `name`                       VARCHAR(128) NOT NULL,
    `depth`                      INT          NOT NULL,
    `parent_code`                VARCHAR(16),
    `content_type_id`            VARCHAR(16),
    `content_type_id_multi_lang` VARCHAR(16),
    `description`                VARCHAR(512),
    `active`                     TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`                 DATETIME(6),
    `updated_at`                 DATETIME(6),
    `deleted_at`                 DATETIME(6),
    INDEX `idx_tour_categories_content_type_id` (`content_type_id`),
    INDEX `idx_tour_categories_content_type_id_multi_lang` (`content_type_id_multi_lang`),
    CONSTRAINT `fk_tour_categories_content_type_id` FOREIGN KEY (`content_type_id`) REFERENCES `tour_content_types` (`content_type_id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_tour_categories_parent_code` FOREIGN KEY (`parent_code`) REFERENCES `tour_categories` (`code`) ON UPDATE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tours`
(
    `content_id`       VARCHAR(32)  NOT NULL,
    `content_type_id`  VARCHAR(16),
    `title`            VARCHAR(256) NOT NULL,
    `tel`              VARCHAR(128),
    `addr1`            VARCHAR(256),
    `addr2`            VARCHAR(256),
    `zipcode`          VARCHAR(16),
    `area_code`        VARCHAR(16),
    `sigungu_code`     VARCHAR(16),
    `cat1`             VARCHAR(16),
    `cat2`             VARCHAR(16),
    `cat3`             VARCHAR(16),
    `event_start_date` VARCHAR(16),
    `event_end_date`   VARCHAR(16),
    `first_image`      VARCHAR(512),
    `first_image2`     VARCHAR(512),
    `cpyrht_div_cd`    VARCHAR(32),
    `map_x`            DOUBLE,
    `map_y`            DOUBLE,
    `m_level`          VARCHAR(8),
    `l_dong_regn_cd`   VARCHAR(16),
    `l_dong_signgu_cd` VARCHAR(16),
    `lcls_systm1`      VARCHAR(32),
    `lcls_systm2`      VARCHAR(32),
    `lcls_systm3`      VARCHAR(32),
    `progress_type`    VARCHAR(32),
    `festival_type`    VARCHAR(32),
    `created_time`     VARCHAR(16),
    `modified_time`    VARCHAR(16),
    `created_at`       DATETIME(6),
    `updated_at`       DATETIME(6),
    `deleted_at`       DATETIME(6),
    INDEX `idx_tour_event_date` (`event_start_date`, `event_end_date`),
    INDEX `idx_tour_area` (`area_code`, `sigungu_code`),
    PRIMARY KEY (`content_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `itinerary_filter_settings`
(
    `id`                         VARCHAR(36) NOT NULL,
    `max_itineraries_per_day`    INT         NOT NULL DEFAULT 5,
    `max_distance_km`            INT         NOT NULL DEFAULT 10,
    `allow_category_mix_per_day` TINYINT(1)  NOT NULL DEFAULT 1,
    `is_global`                  TINYINT(1)  NOT NULL DEFAULT 1,
    `created_at`                 DATETIME(6),
    `updated_at`                 DATETIME(6),
    `deleted_at`                 DATETIME(6),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE itineraries
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `start_date` DATETIME(6) NOT NULL,
    `end_date`   DATETIME(6) NOT NULL,
    `status`     VARCHAR(16) NOT NULL,
    `tour_id`    VARCHAR(32) NULL,
    `user_id`    BIGINT      NOT NULL,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    CONSTRAINT fk_itineraries_tour FOREIGN KEY (tour_id) REFERENCES tours (content_id) ON DELETE SET NULL,
    CONSTRAINT fk_itineraries_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_itineraries_status (status),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE itinerary_days
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT,
    `itinerary_id` BIGINT NOT NULL,
    `date`         DATE   NOT NULL,
    `day_number`   INT    NOT NULL,
    `created_at`   DATETIME(6),
    `updated_at`   DATETIME(6),
    `deleted_at`   DATETIME(6),
    CONSTRAINT fk_itinerary_days_itinerary FOREIGN KEY (`itinerary_id`) REFERENCES itineraries (`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE itinerary_tours
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `itinerary_day_id` BIGINT       NOT NULL,
    `content_id`       VARCHAR(256) NOT NULL,
    `sequence`         INT          NOT NULL,
    `created_at`       DATETIME(6),
    `updated_at`       DATETIME(6),
    `deleted_at`       DATETIME(6),
    CONSTRAINT fk_itinerary_venues_day FOREIGN KEY (`itinerary_day_id`) REFERENCES itinerary_days (`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

SET @@foreign_key_checks = 1;
