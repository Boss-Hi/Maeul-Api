CREATE TABLE missions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(256)  NOT NULL UNIQUE,
    name        VARCHAR(256)  NOT NULL,
    description VARCHAR(2048) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mission_tasks
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(256)  NOT NULL,
    description VARCHAR(2048) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mission_task_mappings
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id      BIGINT NOT NULL,
    mission_task_id BIGINT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mission_task_mapping_mission FOREIGN KEY (mission_id) REFERENCES missions (id) ON DELETE CASCADE,
    CONSTRAINT fk_mission_task_mapping_task FOREIGN KEY (mission_task_id) REFERENCES mission_tasks (id) ON DELETE CASCADE
);

CREATE TABLE mission_triggers
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    type        VARCHAR(32)   NOT NULL,
    name        VARCHAR(256)  NOT NULL,
    value       VARCHAR(256)  NOT NULL,
    description VARCHAR(2048) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME DEFAULT CURRENT_TIMESTAMP

);

CREATE TABLE mission_trigger_mappings
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id         BIGINT        NOT NULL,
    mission_trigger_id BIGINT        NOT NULL,
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mission_trigger_map_mission FOREIGN KEY (mission_id) REFERENCES missions (id) ON DELETE CASCADE,
    CONSTRAINT fk_mission_trigger_map_trigger FOREIGN KEY (mission_trigger_id) REFERENCES mission_triggers (id) ON DELETE CASCADE
);

CREATE TABLE mission_task_verifications
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(32)   NOT NULL,
    type        VARCHAR(32)   NOT NULL,
    value       VARCHAR(256)  NOT NULL,
    description VARCHAR(2048) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mission_task_verification_mappings
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_task_id              BIGINT NOT NULL,
    mission_task_verification_id BIGINT NOT NULL,
    created_at                   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at                   DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mission_task_verification_map_task FOREIGN KEY (mission_task_id) REFERENCES mission_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_mission_task_verification_map_verification FOREIGN KEY (mission_task_verification_id) REFERENCES mission_task_verifications (id) ON DELETE CASCADE
);

CREATE TABLE itinerary_mission_mappings
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(256)  NOT NULL,
    description  VARCHAR(2048) NOT NULL,
    status       VARCHAR(16)   NOT NULL,
    itinerary_id BIGINT        NOT NULL,
    mission_id   BIGINT        NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_itin_mission_map_itin_mission FOREIGN KEY (itinerary_id) REFERENCES itineraries (id) ON DELETE CASCADE,
    CONSTRAINT fk_itin_mission_map_mission FOREIGN KEY (mission_id) REFERENCES missions (id) ON DELETE CASCADE,
    INDEX idx_itin_mission_map_status (status)
);

CREATE TABLE itinerary_mission_task_mappings
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                         VARCHAR(256)  NOT NULL,
    description                  VARCHAR(2048) NOT NULL,
    is_complete                  BOOLEAN       NOT NULL,
    itinerary_mission_mapping_id BIGINT        NOT NULL,
    mission_task_id              BIGINT        NOT NULL,
    created_at                   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at                   DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_itin_mission_task_map_itin_mission_map FOREIGN KEY (itinerary_mission_mapping_id) REFERENCES itinerary_mission_mappings (id) ON DELETE CASCADE,
    CONSTRAINT fk_itin_mission_task_map_mission_task FOREIGN KEY (mission_task_id) REFERENCES mission_tasks (id) ON DELETE CASCADE
);

CREATE TABLE itinerary_mission_task_verification_mappings
(
    id                                BIGINT AUTO_INCREMENT PRIMARY KEY,
    type                              VARCHAR(32)  NOT NULL,
    name                              VARCHAR(256) NOT NULL,
    value                             VARCHAR(256) NOT NULL,
    itinerary_mission_task_mapping_id BIGINT       NOT NULL,
    mission_task_verification_id      BIGINT       NOT NULL,
    created_at                        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at                        DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_itin_mission_task_ver_map_itin_mission_task_map FOREIGN KEY (itinerary_mission_task_mapping_id) REFERENCES itinerary_mission_task_mappings (id) ON DELETE CASCADE,
    CONSTRAINT fk_itin_mission_task_ver_map_mission_task_ver FOREIGN KEY (mission_task_verification_id) REFERENCES mission_task_verifications (id) ON DELETE CASCADE
);