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
    private final int MAX_MISSIONS = 1;

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
                    for (ItinerarySchedule iTour : day.getItineraryTours()) {
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
        Set<Mission> targetMissions = new HashSet<>();

        for (MissionTrigger trigger : matchedTriggers) {
            trigger.getMissionTriggerMappings().forEach(mapping -> targetMissions.add(mapping.getMission()));
        }

        // 4. Itinerary 엔티티에 미션 매핑 추가
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
                // 4-1. 루트 미션 매핑 엔티티 생성
                ItineraryMissionMapping newMapping = ItineraryMissionMapping.builder()
                        .itinerary(itinerary)
                        .mission(mission)
                        .name(mission.getName())
                        .description(mission.getDescription())
                        // 하위 컬렉션 NPE 방지용 빈 리스트 초기화 (엔티티에 @Builder.Default 가 없을 경우를 대비)
                        .itineraryMissionTaskMappings(new ArrayList<>())
                        .build();

                // 4-2. 미션의 하위 Task들을 순회하며 Task 매핑 엔티티 생성[cite: 15]
                if (mission.getMissionTasks() != null) {
                    for (MissionTask task : mission.getMissionTasks()) {
                        ItineraryMissionTaskMapping taskMapping = ItineraryMissionTaskMapping.builder()
                                .name(task.getName())
                                .description(task.getDescription())
                                .isComplete(false) // 기본 완료 상태 false 할당[cite: 15]
                                .itineraryMissionMapping(newMapping)
                                .missionTask(task)
                                .itineraryMissionTaskVerificationMappings(new ArrayList<>())
                                .build();

                        // 4-3. Task의 하위 Verification들을 순회하며 Verification 매핑 엔티티 생성[cite: 16]
                        if (task.getMissionTaskVerifications() != null) {
                            for (MissionTaskVerification verification : task.getMissionTaskVerifications()) {
                                ItineraryMissionTaskVerificationMapping verificationMapping = ItineraryMissionTaskVerificationMapping.builder()
                                        .name(verification.getName())
                                        .description(verification.getDescription())
                                        .type(verification.getType())
                                        .itineraryMissionTaskMapping(taskMapping)
                                        .missionTaskVerification(verification)
                                        .build();

                                taskMapping.getItineraryMissionTaskVerificationMappings().add(verificationMapping);
                            }
                        }

                        newMapping.getItineraryMissionTaskMappings().add(taskMapping);
                    }
                }

                itinerary.getItineraryMissionMappings().add(newMapping);
                currentMissionCount++;
            }
        }

        // 5. 변경된 Itinerary 저장 (Cascade 설정에 의해 하위 Task 및 Verification 매핑도 자동 저장됨)
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