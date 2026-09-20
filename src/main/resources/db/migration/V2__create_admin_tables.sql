CREATE TABLE IF NOT EXISTS `admin_users`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `email`      VARCHAR(256) NOT NULL UNIQUE,
    `password`   VARCHAR(256) NOT NULL,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `deleted_at` DATETIME(6),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS event_publication
(
    id               BINARY(16)    NOT NULL,
    completion_date  DATETIME(6),
    event_type       VARCHAR(512)  NOT NULL,
    listener_id      VARCHAR(512)  NOT NULL,
    publication_date DATETIME(6)   NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;