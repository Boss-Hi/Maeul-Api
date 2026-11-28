-- Spring Modulith 2.x 이벤트 발행 기록(event_publication)이 쓰는 컬럼을 추가한다.
-- V2 의 테이블은 Modulith 1.x 모양이라 @TransactionalEventListener 의 발행 기록을 저장할 때 "Unknown column 'completion_attempts'" 로 실패한다.
ALTER TABLE event_publication
    ADD COLUMN status                 VARCHAR(20) NULL,
    ADD COLUMN completion_attempts    INT         NOT NULL DEFAULT 0,
    ADD COLUMN last_resubmission_date DATETIME(6) NULL;

-- 이미 쌓인 기록의 상태를 채운다 (완료 시각이 있으면 COMPLETED)
UPDATE event_publication
SET status = CASE WHEN completion_date IS NULL THEN 'PUBLISHED' ELSE 'COMPLETED' END
WHERE status IS NULL;
