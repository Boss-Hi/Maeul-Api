package com.bosshi.maeul.itinerary.kafka.consumer;

import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.kafka.ItineraryGroup;
import com.bosshi.maeul.itinerary.kafka.ItineraryTopic;
import com.bosshi.maeul.itinerary.response.ItineraryGenerateResult;
import com.bosshi.maeul.itinerary.service.ItineraryMissionService;
import com.bosshi.maeul.itinerary.service.ItineraryService;
import com.bosshi.maeul.websocket.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryGenerateConsumer {
    private static final String FAILURE_MESSAGE = "일정 생성에 실패했습니다. 잠시 후 다시 시도해주세요.";

    private final ItineraryService itineraryService;
    private final ItineraryMissionService itineraryMissionService;

    // 인터페이스 타입으로 주입 받음
    private final MessageSender messageSender;

    @KafkaListener(topics = ItineraryTopic.ItineraryGenerate, groupId = ItineraryGroup.ItineraryGroup)
    public void consumeGenerateEvent(ItineraryGenerateDTO dto) {
        try {
            log.info("Kafka 이벤트 수신 (일정 생성 시작) - UserID: {}", dto.getUserId());

            // 1. 일정 생성
            Itinerary itinerary = itineraryService.generateItinerary(dto);

            // 2. 미션 생성
            itineraryMissionService.generateMissionsForItinerary(itinerary);

            log.info("Kafka 이벤트 처리 완료 - ItineraryID: {}", itinerary.getId());

            // 3. STOMP 웹소켓을 통해 해당 사용자에게 실시간 결과 전송
            // 프론트엔드 구독 경로: /private/queue/itinerary-generate
            messageSender.sendToUser(dto.getUserId(), ItineraryTopic.ItineraryGenerate, ItineraryGenerateResult.success(itinerary.getId()));
        } catch (Exception e) {
            log.error("일정 생성 비동기 처리 중 오류 발생: {}", e.getMessage(), e);
            notifyFailure(dto.getUserId(), e);
            // TODO: 실패 시 보상 트랜잭션(Saga) 또는 Dead Letter Queue(DLQ) 처리
        }
    }

    private void notifyFailure(Long userId, Exception cause) {
        String message = cause instanceof ServiceUnavailableException
                ? cause.getMessage()
                : FAILURE_MESSAGE;
        try {
            messageSender.sendToUser(userId, ItineraryTopic.ItineraryGenerate, ItineraryGenerateResult.failed(message));
        } catch (Exception sendError) {
            log.error("일정 생성 실패 알림 전송 실패 - UserID: {}", userId, sendError);
        }
    }
}