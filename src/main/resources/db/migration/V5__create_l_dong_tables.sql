-- 1. 부모 테이블: 법정동 시/도 (Region)
CREATE TABLE l_dong_regions
(
    code       VARCHAR(10) PRIMARY KEY COMMENT '시/도 코드 (예: 11)',
    name       VARCHAR(50) NOT NULL COMMENT '시/도 명칭 (예: 서울특별시)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시'
);

-- 2. 자식 테이블: 법정동 시/군/구 (Sigungu)
CREATE TABLE l_dong_sigungus
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_code VARCHAR(10) NOT NULL COMMENT '부모 시/도 코드 (FK)',
    code        VARCHAR(10) NOT NULL COMMENT '시/군/구 코드 (예: 110, 140)',
    name        VARCHAR(50) NOT NULL COMMENT '시/군/구 명칭 (예: 종로구, 중구)',
    rnum        INT                  DEFAULT NULL COMMENT '정렬/행 번호',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',

    CONSTRAINT fk_l_dong_sigungu_region
        FOREIGN KEY (region_code) REFERENCES l_dong_regions (code)
);

-- 3. 검색 성능 최적화를 위한 복합 유니크 제약조건 및 인덱스
-- 동일 시/도 내 시/군/구 코드는 유일해야 함
ALTER TABLE l_dong_sigungus
    ADD CONSTRAINT uk_region_sigungu_code UNIQUE (region_code, code);

-- 정렬 순서(rnum) 조회용 인덱스
CREATE INDEX idx_l_dong_sigungu_rnum ON l_dong_sigungus (rnum);