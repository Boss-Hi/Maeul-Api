package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.entity.*;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itinerary.repository.MissionTriggerRepository;
import com.bosshi.maeul.itinerary.type.MissionTriggerType;
import com.bosshi.maeul.openapi.entity.Tour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryMissionService {

    private final MissionTriggerRepository missionTriggerRepository;
    private final ItineraryRepository itineraryRepository;
    private final int MAX_MISSIONS = 3;

    /**
     * 일정이 생성되었을 때, 일정 내의 관광지(Tour) 카테고리를 분석하여
     * 관련된 미션을 찾아 Itinerary와 연관(매핑)시켜 저장합니다.
     *
     * @param itinerary 생성된(또는 저장된) 일정 엔티티
     */
    @Transactional
    public void generateMissionsForItinerary(Itinerary itinerary) {
        log.info("[ItineraryMissionService] 일정 ID [{}]에 대한 미션 생성 시작...", itinerary.getId());

        // 1. 일정에 포함된 모든 Tour 객체의 카테고리 코드(cat1, cat2, cat3) 수집
        Set<String> categoryCodes = new HashSet<>();

        if (itinerary.getItineraryDays() != null) {
            for (ItineraryDay day : itinerary.getItineraryDays()) {
                if (day.getItineraryTours() != null) {
                    for (ItineraryTour iTour : day.getItineraryTours()) {
                        Tour tour = iTour.getTour();
                        if (tour != null) {
                            if (tour.getLclsSystm1() != null) categoryCodes.add(tour.getLclsSystm1());
                            if (tour.getLclsSystm2() != null) categoryCodes.add(tour.getLclsSystm2());
                            if (tour.getLclsSystm3() != null) categoryCodes.add(tour.getLclsSystm3());
                        }
                    }
                }
            }
        }

        // 공통 미션 코드(온보딩) 강제 주입
        categoryCodes.add("ONB");

        if (categoryCodes.isEmpty()) {
            log.warn("[ItineraryMissionService] 일정 ID [{}]에 관광지 카테고리 정보가 없습니다.", itinerary.getId());
            return;
        }

        // 2. 수집된 카테고리 코드들과 일치하는 카테고리형 MissionTrigger 조회
        List<MissionTrigger> matchedTriggers = missionTriggerRepository.findByTypeAndValueIn(
                MissionTriggerType.CATEGORY,
                new ArrayList<>(categoryCodes)
        );

        if (matchedTriggers.isEmpty()) {
            log.info("[ItineraryMissionService] 매칭되는 미션 트리거가 없습니다.");
            return;
        }

        // 3. 찾은 트리거를 기반으로 연관된 Mission 추출 (중복 제거)
        // MissionTrigger가 속한 MissionTriggerMapping을 통해 원본 Mission 객체들을 가져옵니다.
        Set<Mission> targetMissions = new HashSet<>();

        // (주의: 이 로직이 정상 작동하려면 MissionTrigger 엔티티에 MissionTriggerMapping 역방향 참조가 있거나,
        // 별도의 쿼리로 Trigger를 가진 Mission들을 조회해야 합니다.
        // 만약 MissionTriggerRepository 에서 바로 Mission을 조회하는 커스텀 쿼리를 만드셨다면 그걸 쓰는 것이 가장 좋습니다.
        // 여기서는 예시로 DB 조회 편의상 MissionRepository (또는 MappingRepository) 쿼리를 호출한다고 가정합니다.)

        // 아래는 이미 연관관계 매핑이 되어있다고 가정했을 때의 논리적 흐름입니다.
        for (MissionTrigger trigger : matchedTriggers) {
            trigger.getMissionTriggerMappings().forEach(mapping -> targetMissions.add(mapping.getMission()));
        }

        // 만약 위 역방향 참조(mappedBy)를 만들지 않았다면, 서비스 단에서 아래처럼 수동으로 세팅할 타겟 미션들을 선별합니다.
        // ※ 실제 구현 시에는 "List<Mission> findMissionsByTriggerIds(List<Long> triggerIds)" 같은 JPQL을 리포지토리에 추가하는 것을 권장합니다.

        // 4. Itinerary 엔티티에 미션 매핑 추가
        // Itinerary가 직접 ItineraryMissionMapping 리스트를 관리하므로,
        // 찾아낸 Mission 객체들을 매핑 엔티티로 포장하여 리스트에 더해줍니다.
        if (itinerary.getItineraryMissionMappings() == null) {
            itinerary.setItineraryMissionMappings(new ArrayList<>());
        }

        // 현재 등록된 미션 개수 확인
        int currentMissionCount = itinerary.getItineraryMissionMappings().size();

        for (Mission mission : targetMissions) {
            if (currentMissionCount >= MAX_MISSIONS) {
                break;
            }
            // 중복 방지 방어 코드: 이미 해당 미션이 매핑되어 있는지 확인
            boolean alreadyExists = itinerary.getItineraryMissionMappings().stream()
                    .anyMatch(mapping -> mapping.getMission().getId().equals(mission.getId()));

            if (!alreadyExists) {
                ItineraryMissionMapping newMapping = ItineraryMissionMapping.builder()
                        .itinerary(itinerary)
                        .mission(mission)
                        .build();
                itinerary.getItineraryMissionMappings().add(newMapping);

                // 정상적으로 매핑되었을 때 카운트 증가
                currentMissionCount++;
            }
        }

        // 5. 변경된 Itinerary 저장 (Cascade 설정에 의해 Mapping 엔티티도 자동 저장됨)[cite: 23]
        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        log.info(
                "[ItineraryMissionService] 일정 ID [{}] 미션 생성 완료. 총 {}개의 미션이 매핑됨.",
                savedItinerary.getId(), savedItinerary.getItineraryMissionMappings().size()
        );
    }

    public List<Mission> getMissionsForItinerary(Long itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid itinerary ID: " + itineraryId));

        return itinerary.getMissions();
    }

    public Optional<Itinerary> getCurrentItineraryByUserId(Long id) {
        return itineraryRepository.findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateDesc(id, LocalDate.now());
    }
}