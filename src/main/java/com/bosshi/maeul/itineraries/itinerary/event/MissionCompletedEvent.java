package com.bosshi.maeul.itineraries.itinerary.event;

import org.springframework.modulith.NamedInterface;

/**
 * 사용자가 일정에 딸린 미션을 완료했을 때 보낸다. 미션 완료 기능을 만들 때 발행한다 (ADR 0014).
 * 시군구는 그 일정 주축제의 시도 코드(regionCode)와 시군구 코드(subdivisionCode)이며, 없으면 둘 다 null 이다.
 *
 * @param itineraryMissionId 완료한 미션 (itinerary_mission_mappings.id)
 */
@NamedInterface
public record MissionCompletedEvent(
        Long userId,
        Long itineraryMissionId,
        String regionCode,
        String subdivisionCode
) {
}
