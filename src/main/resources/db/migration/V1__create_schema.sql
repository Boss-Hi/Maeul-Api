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
  DEFAULT CHARSET = utf8mb4;

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
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `tour_content_types`
(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `content_type_id` VARCHAR(16) NOT NULL UNIQUE,
    `content_type_id_multi_lang` VARCHAR(16),
    `name`            VARCHAR(32) NOT NULL,
    `active`          TINYINT(1)  NOT NULL DEFAULT 1,
    `created_at`      DATETIME(6),
    `updated_at`      DATETIME(6),
    `deleted_at`      DATETIME(6),
    INDEX `idx_tour_content_types_content_type_id` (`content_type_id`),
    INDEX `idx_tour_content_types_content_type_id_multi_lang` (`content_type_id_multi_lang`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

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
  DEFAULT CHARSET = utf8mb4;

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
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `main_festivals`
(
    `id`         VARCHAR(36)  NOT NULL,
    `content_id` VARCHAR(255) NOT NULL UNIQUE,
    `title`      VARCHAR(255) NOT NULL,
    `category`   VARCHAR(255) NOT NULL,
    `start_date` DATE         NOT NULL,
    `end_date`   DATE         NOT NULL,
    `latitude`   DOUBLE       NOT NULL,
    `longitude`  DOUBLE       NOT NULL,
    `address1`   VARCHAR(500),
    `address2`   VARCHAR(500),
    `tel`        VARCHAR(50),
    `image_url`  VARCHAR(1000),
    `area_code`  VARCHAR(10),
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `trips`
(
    `id`                  VARCHAR(36)  NOT NULL,
    `gender`              VARCHAR(255) NOT NULL,
    `birth_date`          DATE         NOT NULL,
    `destination`         VARCHAR(255) NOT NULL,
    `start_date`          DATE         NOT NULL,
    `end_date`            DATE         NOT NULL,
    `selected_categories` VARCHAR(500) NOT NULL,
    `main_festival_id`    VARCHAR(36),
    `filter_settings_id`  VARCHAR(36),
    `created_at`          DATETIME(6),
    `updated_at`          DATETIME(6),
    `deleted_at`          DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_trips_main_festival` (`main_festival_id`),
    INDEX `idx_trips_filter_settings` (`filter_settings_id`),
    CONSTRAINT `fk_trips_main_festival` FOREIGN KEY (`main_festival_id`) REFERENCES `main_festivals` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_trips_filter_settings` FOREIGN KEY (`filter_settings_id`) REFERENCES `filter_settings` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `itineraries`
(
    `id`         VARCHAR(36) NOT NULL,
    `trip_id`    VARCHAR(36) NOT NULL UNIQUE,
    `summary`    VARCHAR(1000),
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `deleted_at` DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_itineraries_trip_id` (`trip_id`),
    CONSTRAINT `fk_itineraries_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `itinerary_days`
(
    `id`           VARCHAR(36) NOT NULL,
    `itinerary_id` VARCHAR(36) NOT NULL,
    `date`         DATE        NOT NULL,
    `day_number`   INT         NOT NULL,
    `theme`        VARCHAR(100),
    `created_at`   DATETIME(6),
    `updated_at`   DATETIME(6),
    `deleted_at`   DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_itinerary_days_itinerary_id` (`itinerary_id`),
    CONSTRAINT `fk_itinerary_days_itinerary` FOREIGN KEY (`itinerary_id`) REFERENCES `itineraries` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `itinerary_venues`
(
    `id`               VARCHAR(36)  NOT NULL,
    `itinerary_day_id` VARCHAR(36)  NOT NULL,
    `content_id`       VARCHAR(255) NOT NULL,
    `name`             VARCHAR(255) NOT NULL,
    `category`         VARCHAR(255) NOT NULL,
    `visit_time`       VARCHAR(50),
    `duration`         VARCHAR(50),
    `description`      VARCHAR(500),
    `latitude`         DOUBLE,
    `longitude`        DOUBLE,
    `sequence`         INT          NOT NULL,
    `created_at`       DATETIME(6),
    `updated_at`       DATETIME(6),
    `deleted_at`       DATETIME(6),
    PRIMARY KEY (`id`),
    INDEX `idx_itinerary_venues_day_id` (`itinerary_day_id`),
    CONSTRAINT `fk_itinerary_venues_day` FOREIGN KEY (`itinerary_day_id`) REFERENCES `itinerary_days` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

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
  DEFAULT CHARSET = utf8mb4;

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
  DEFAULT CHARSET = utf8mb4;

SET @@foreign_key_checks = 1;
