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
