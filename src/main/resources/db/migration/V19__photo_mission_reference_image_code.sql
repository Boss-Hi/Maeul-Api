-- 사진 미션: 기준 사진 3장 중 하나라도 맞으면 성공 (ADR 0017).
-- 기준 사진을 URL 이 아니라 base64 코드로 DB 에 둔다 (Seeder 가 만든다). 일정에 기준 사진 하나를 고정하지 않는다.
ALTER TABLE `photo_mission_reference_photos`
    MODIFY `image_url` VARCHAR(500) NULL COMMENT 'URL 로 등록한 기준 사진. image_base64 가 있으면 쓰지 않는다',
    ADD COLUMN `code` VARCHAR(64) NULL COMMENT 'Seeder 가 중복 생성하지 않도록 구분하는 코드',
    ADD COLUMN `mime_type` VARCHAR(32) NULL,
    ADD COLUMN `image_base64` MEDIUMTEXT NULL COMMENT '이미지 파일의 base64',
    ADD UNIQUE KEY `uk_photo_mission_reference_photos_code` (`code`);

ALTER TABLE `photo_missions`
    MODIFY `reference_photo_id` BIGINT NULL COMMENT '더 이상 쓰지 않음. 미션은 장소의 사용 중인 기준 사진 전체와 비교한다';
