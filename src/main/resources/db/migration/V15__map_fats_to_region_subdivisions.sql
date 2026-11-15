-- 병아리의 시군구를 region_subdivisions 행으로 가리키고, 카카오 법정동 코드 표는 없앤다.
-- 점령 시간과는 카카오가 위치 판정 때 준 시도·시군구 이름(territory_region_cells)으로 맞춘다 (ADR 0012).
-- 기존 병아리는 모두 지운다.
DELETE FROM `fats`;

-- fk_fats_user 가 쓸 user_id 인덱스가 남도록 새 인덱스를 먼저 만들고 옛 인덱스를 지운다.
ALTER TABLE `fats`
    ADD COLUMN `region_subdivision_id` BIGINT NOT NULL COMMENT '시군구 (region_subdivisions.id)' AFTER `user_id`,
    ADD KEY `idx_fats_user_region_subdivision` (`user_id`, `region_subdivision_id`),
    ADD CONSTRAINT `fk_fats_region_subdivision` FOREIGN KEY (`region_subdivision_id`) REFERENCES `region_subdivisions` (`id`);

ALTER TABLE `fats`
    DROP FOREIGN KEY `fk_fats_legal_dong_code`;

ALTER TABLE `fats`
    DROP INDEX `uk_fats_user_active_legal_dong_code`,
    DROP INDEX `idx_fats_user_legal_dong_code`,
    DROP INDEX `fk_fats_legal_dong_code`,
    DROP COLUMN `active_legal_dong_code_id`,
    DROP COLUMN `legal_dong_code_id`;

ALTER TABLE `fats`
    ADD COLUMN `active_region_subdivision_id` BIGINT AS (IF(`graduated_at` IS NULL, `region_subdivision_id`, NULL)) STORED COMMENT '키우는 중인 병아리 중복 방지용',
    ADD UNIQUE KEY `uk_fats_user_active_region_subdivision` (`user_id`, `active_region_subdivision_id`);

DROP TABLE `kakao_legal_dong_codes`;
