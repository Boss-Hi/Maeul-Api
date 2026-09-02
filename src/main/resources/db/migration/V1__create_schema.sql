-- Flyway migration: create schema tables based on current JPA entities
-- Generated: 2026-08-27

SET @@foreign_key_checks = 0;

CREATE TABLE IF NOT EXISTS `users`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `email`      VARCHAR(255) NOT NULL UNIQUE,
    `password`   VARCHAR(255) NOT NULL,
    `role`       VARCHAR(50)  NOT NULL,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `profiles`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL UNIQUE,
    `nickname`   VARCHAR(30) NOT NULL,
    `bio`        VARCHAR(200),
    `avatar_url` VARCHAR(255),
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_profiles_user_id` (`user_id`),
    CONSTRAINT `fk_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

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
    `name`                       VARCHAR(100) NOT NULL,
    `depth`                      INT          NOT NULL,
    `parent_code`                VARCHAR(16),
    `content_type_id`            VARCHAR(16),
    `content_type_id_multi_lang` VARCHAR(16),
    `description`                VARCHAR(500),
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
    `content_id`       VARCHAR(20)  NOT NULL,
    `content_type_id`  VARCHAR(10),
    `title`            VARCHAR(255) NOT NULL,
    `tel`              VARCHAR(50),
    `addr1`            VARCHAR(255),
    `addr2`            VARCHAR(255),
    `zipcode`          VARCHAR(10),
    `area_code`        VARCHAR(10),
    `sigungu_code`     VARCHAR(10),
    `cat1`             VARCHAR(10),
    `cat2`             VARCHAR(10),
    `cat3`             VARCHAR(10),
    `event_start_date` VARCHAR(16),
    `event_end_date`   VARCHAR(16),
    `first_image`      VARCHAR(500),
    `first_image2`     VARCHAR(500),
    `cpyrht_div_cd`    VARCHAR(20),
    `map_x`            DOUBLE,
    `map_y`            DOUBLE,
    `m_level`          VARCHAR(5),
    `l_dong_regn_cd`   VARCHAR(10),
    `l_dong_signgu_cd` VARCHAR(10),
    `lcls_systm1`      VARCHAR(20),
    `lcls_systm2`      VARCHAR(20),
    `lcls_systm3`      VARCHAR(20),
    `progress_type`    VARCHAR(20),
    `festival_type`    VARCHAR(20),
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

CREATE TABLE IF NOT EXISTS `filter_settings`
(
    `id`                         VARCHAR(36) NOT NULL,
    `max_venues_per_day`         INT         NOT NULL DEFAULT 5,
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
    `tour_id`    VARCHAR(20) NULL,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    CONSTRAINT fk_itineraries_tour FOREIGN KEY (tour_id) REFERENCES tours (content_id) ON DELETE SET NULL,
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
    `content_id`       VARCHAR(255) NOT NULL,
    `sequence`         INT          NOT NULL,
    `created_at`       DATETIME(6),
    `updated_at`       DATETIME(6),
    `deleted_at`       DATETIME(6),
    CONSTRAINT fk_itinerary_venues_day FOREIGN KEY (`itinerary_day_id`) REFERENCES itinerary_days (`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `posts`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `category`   VARCHAR(50)  NOT NULL,
    `title`      VARCHAR(100) NOT NULL,
    `content`    TEXT         NOT NULL,
    `view_count` BIGINT       NOT NULL DEFAULT 0,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_posts_user_id` (`user_id`),
    CONSTRAINT `fk_posts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `comments`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `post_id`           BIGINT       NOT NULL,
    `user_id`           BIGINT       NOT NULL,
    `parent_comment_id` BIGINT,
    `content`           VARCHAR(500) NOT NULL,
    `created_at`        DATETIME(6),
    `updated_at`        DATETIME(6),
    `deleted_at`        DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_comments_post_id` (`post_id`),
    INDEX `idx_comments_user_id` (`user_id`),
    INDEX `idx_comments_parent` (`parent_comment_id`),
    CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

SET @@foreign_key_checks = 1;
