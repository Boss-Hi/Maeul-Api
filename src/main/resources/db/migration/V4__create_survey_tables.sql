-- 1. 설문 마스터 테이블
CREATE TABLE IF NOT EXISTS `surveys`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NOT NULL UNIQUE, -- 예: 'MAEUL_TASTE'
    `title`       VARCHAR(256) NOT NULL,
    `description` VARCHAR(512),
    `active`      TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`  DATETIME,
    `updated_at`  DATETIME,
    `deleted_at`  DATETIME,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 2. 설문 문항(질문) 테이블
CREATE TABLE IF NOT EXISTS `survey_questions`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `survey_id`     BIGINT       NOT NULL,
    `step_order`    INT          NOT NULL,                         -- 화면 진행 순서 (1, 2, ...)
    `step_badge`    VARCHAR(32),                                   -- 예: 'BASIC 01', 'BASIC 02'
    `title`         VARCHAR(256) NOT NULL,                         -- 예: '나이대를 알려주세요'
    `sub_title`     VARCHAR(512),                                  -- 예: '비슷한 체류 리듬과 관심사를...'
    `question_type` VARCHAR(16)  NOT NULL DEFAULT 'SINGLE_CHOICE', -- 'SINGLE_CHOICE', 'MULTIPLE_CHOICE'
    `is_required`   TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`    DATETIME,
    `updated_at`    DATETIME,
    `deleted_at`    DATETIME,
    INDEX `idx_survey_questions_survey_id` (`survey_id`),
    CONSTRAINT `fk_survey_questions_survey_id` FOREIGN KEY (`survey_id`) REFERENCES `surveys` (`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 3. 문항별 선택지 옵션 테이블
CREATE TABLE IF NOT EXISTS `survey_question_options`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `question_id`  BIGINT       NOT NULL,
    `option_order` INT          NOT NULL, -- 옵션 표출 순서 (1, 2, ...)
    `label`        VARCHAR(128) NOT NULL, -- 예: '10대', '직장인'
    `sub_label`    VARCHAR(256),          -- 예: '퇴근 후 일정과 주말 체류에 잘 맞아요.'
    `value`        VARCHAR(32)  NOT NULL, -- 예: 'AGE_10', 'JOB_WORKER'
    `icon_url`     VARCHAR(512),          -- 아이콘 이미지 URL
    `created_at`   DATETIME,
    `updated_at`   DATETIME,
    `deleted_at`   DATETIME,
    INDEX `idx_survey_question_options_question_id` (`question_id`),
    CONSTRAINT `fk_survey_question_options_question_id` FOREIGN KEY (`question_id`) REFERENCES `survey_questions` (`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 1. 설문 응답 (Master) 테이블 생성
CREATE TABLE survey_responses
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id    BIGINT NOT NULL COMMENT '설문 ID',
    user_id      BIGINT NOT NULL COMMENT '회원 ID',
    `created_at` DATETIME,
    `updated_at` DATETIME,
    `deleted_at` DATETIME,

    INDEX `idx_survey_response_survey_id` (`survey_id`),
    INDEX `idx_survey_response_user_id` (`user_id`),
    CONSTRAINT fk_survey_response_survey FOREIGN KEY (survey_id) REFERENCES surveys (id)
);

-- 2. 질문별 선택 답변 (Detail) 테이블 생성
CREATE TABLE survey_answers
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    response_id  BIGINT NOT NULL COMMENT '설문 응답 ID',
    question_id  BIGINT NOT NULL COMMENT '설문 질문 ID',
    option_id    BIGINT NOT NULL COMMENT '설문 옵션 ID',
    `created_at` DATETIME,
    `updated_at` DATETIME,
    `deleted_at` DATETIME,

    INDEX `idx_survey_answer_response_id` (`response_id`),
    INDEX `idx_survey_answer_question_id` (`question_id`),
    INDEX `idx_survey_answer_option_id` (`option_id`),
    CONSTRAINT fk_survey_answer_response
        FOREIGN KEY (response_id) REFERENCES survey_responses (id) ON DELETE CASCADE,
    CONSTRAINT fk_survey_answer_question
        FOREIGN KEY (question_id) REFERENCES survey_questions (id),
    CONSTRAINT fk_survey_answer_option
        FOREIGN KEY (option_id) REFERENCES survey_question_options (id)
);