-- 1. 부모 테이블: 법정동 시/도 (Region)
CREATE TABLE regions
(
    code       VARCHAR(10) PRIMARY KEY COMMENT '시/도 코드 (예: 11)',
    name       VARCHAR(50) NOT NULL COMMENT '시/도 명칭 (예: 서울특별시)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시'
);

-- 2. 자식 테이블: 법정동 시/군/구 (Sigungu)
CREATE TABLE region_subdivisions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_code VARCHAR(10) NOT NULL COMMENT '부모 시/도 코드 (FK)',
    code        VARCHAR(10) NOT NULL COMMENT '시/군/구 코드 (예: 110, 140)',
    name        VARCHAR(50) NOT NULL COMMENT '시/군/구 명칭 (예: 종로구, 중구)',
    rnum        INT                  DEFAULT NULL COMMENT '정렬/행 번호',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',

    CONSTRAINT fk_region_subdivision_region FOREIGN KEY (region_code) REFERENCES regions (code),
    CONSTRAINT uk_region_subdivision_code UNIQUE (region_code, code)
);