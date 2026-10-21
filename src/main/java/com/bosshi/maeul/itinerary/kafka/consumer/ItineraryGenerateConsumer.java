package com.bosshi.maeul.itinerary.kafka.consumer;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.kafka.ItineraryGroup;
import com.bosshi.maeul.itinerary.kafka.ItineraryTopics;
import com.bosshi.maeul.itinerary.service.ItineraryMissionService;
import com.bosshi.maeul.itinerary.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryGenerateConsumer {

    private final ItineraryService itineraryService;
    private final ItineraryMissionService itineraryMissionService;

    @KafkaListener(topics = ItineraryTopics.ItineraryGenerateEvent, groupId = ItineraryGroup.ItineraryGroup)
    public void consumeGenerateEvent(ItineraryGenerateDTO dto) {
        try {
            log.info("Kafka 이벤트 수신 (일정 생성 시작) - UserID: {}", dto.getUserId());

            // 1. 일정 생성
            Itinerary itinerary = itineraryService.generateItinerary(dto);

            // 2. 미션 생성
            itineraryMissionService.generateMissionsForItinerary(itinerary);

            log.info("Kafka 이벤트 처리 완료 - ItineraryID: {}", itinerary.getId());

            // TODO: (선택) 처리가 완료되었음을 FCM 푸시 알림, SSE, 또는 WebSocket 등을 통해 클라이언트에게 알림
        } catch (Exception e) {
            log.error("일정 생성 비동기 처리 중 오류 발생: {}", e.getMessage(), e);
            // TODO: 실패 시 보상 트랜잭션(Saga) 또는 Dead Letter Queue(DLQ) 처리
        }
    }
}