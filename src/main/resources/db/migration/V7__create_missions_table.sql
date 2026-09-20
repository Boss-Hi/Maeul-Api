-- 1. 미션 (Mission)
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

-- 2. 미션 트리거 (Mission Trigger)
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

-- 3. 미션 트리거 매핑 (Mission Trigger Mapping - 중간 테이블)
CREATE TABLE mission_trigger_mappings
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id         BIGINT NOT NULL,
    mission_trigger_id BIGINT NOT NULL,
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mission_trigger_map_mission FOREIGN KEY (mission_id) REFERENCES missions (id) ON DELETE CASCADE,
    CONSTRAINT fk_mission_trigger_map_trigger FOREIGN KEY (mission_trigger_id) REFERENCES mission_triggers (id) ON DELETE CASCADE
);

-- 4. 미션 검증 (Mission Verification)
CREATE TABLE mission_verifications
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    type        VARCHAR(32)   NOT NULL,
    value       VARCHAR(256)  NOT NULL,
    description VARCHAR(2048) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 5. 일정 미션 매핑 (Itinerary Mission Mapping - 중간 테이블)
CREATE TABLE itinerary_mission_mappings
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    status       VARCHAR(16) NOT NULL,
    itinerary_id BIGINT      NOT NULL,
    mission_id   BIGINT      NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_itin_mission_map_itin_mission FOREIGN KEY (itinerary_id) REFERENCES itineraries (id) ON DELETE CASCADE,
    CONSTRAINT fk_itin_mission_map_mission FOREIGN KEY (mission_id) REFERENCES missions (id) ON DELETE CASCADE,
    INDEX idx_itin_mission_map_status (status)
);