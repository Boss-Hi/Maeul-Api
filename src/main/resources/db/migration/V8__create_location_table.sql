CREATE TABLE IF NOT EXISTS `location_mapping_tables`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `target`        VARCHAR(32)  NOT NULL COMMENT 'morph type: USER, ITINERARY_SCHEDULE',
    `target_id`     BIGINT       NOT NULL COMMENT 'morph id',
    `address_line1` VARCHAR(256),
    `address_line2` VARCHAR(256),
    `zipcode`       VARCHAR(16),
    `latitude`      DOUBLE,
    `longitude`     DOUBLE,
    `region1_depth` VARCHAR(64),
    `region2_depth` VARCHAR(64),
    `created_at`    DATETIME(6),
    `updated_at`    DATETIME(6),
    `deleted_at`    DATETIME(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_location_mapping_target` (`target`, `target_id`),
    KEY `idx_location_mapping_target` (`target`, `target_id`),
    KEY `idx_location_mapping_lat_lng` (`latitude`, `longitude`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
