package com.bosshi.maeul.itinerary.controller;

import com.bosshi.maeul.ai.service.GeminiService;
import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.kafka.ItineraryTopic;
import com.bosshi.maeul.itinerary.request.ItineraryGenerateRequest;
import com.bosshi.maeul.kafka.MessageProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/itinerary/kafka")
@RequiredArgsConstructor
@Slf4j
public class ItineraryKafkaController {
    private final MessageProducer messageProducer;
    private final GeminiService geminiService;

    /**
     * @param request MainFestival
     * @return 생성 요청 결과
     */
    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> generate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ItineraryGenerateRequest request
    ) {
        // 사용 가능한 AI 키가 없으면 비동기 처리로 넘기지 않고 즉시 503 응답
        if (!geminiService.hasAvailableApiKey()) {
            throw new ServiceUnavailableException("일정 생성 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.");
        }

        ItineraryGenerateDTO dto = ItineraryGenerateDTO.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .tourId(request.getTourId())
                .userId(userDetails.getId())
                .selectedCategories(List.of("VE06", "EV02", "AC01"))
                .filterSettings(new ItineraryGenerateDTO.FilterSettingsRequest(
                        5,
                        25,
                        true
                ))
                .build();

        messageProducer.sendMessage(ItineraryTopic.ItineraryGenerate, dto);

        return ApiResponse.success("일정 생성 요청이 성공적으로 처리되었습니다.", null);
    }

}


