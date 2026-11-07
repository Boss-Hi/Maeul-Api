package com.bosshi.maeul.timeline.response;

/**
 * @param accepted 저장한 기록 수
 * @param skipped  수집 주기보다 촘촘하거나, 이미 있거나, 미래 시각이라 버린 기록 수
 */
public record TimelineRecordResponse(int accepted, int skipped) {
}
