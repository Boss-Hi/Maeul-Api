package com.bosshi.maeul.itinerary.response;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryMissionMapping;
import com.bosshi.maeul.itinerary.type.MissionStatus;
import com.bosshi.maeul.openapi.response.TourResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.random.RandomGenerator;

@Getter
@Builder
public class CurrentItineraryResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int remainingDay;

    private int neighborhoodCount;

    private TourResponse tour;
    private List<ItineraryMissionMappingResponse> missions;

    public static CurrentItineraryResponse from(Itinerary itinerary) {
        if (itinerary == null) {
            return null;
        }

        return CurrentItineraryResponse.builder()
                .id(itinerary.getId())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .remainingDay((int) (itinerary.getEndDate().toEpochDay() - LocalDate.now().toEpochDay()))
                // @Todo: 임시로 neighborhoodCount를 랜덤으로 설정, 추후 실제 데이터로 변경 필요
                .neighborhoodCount(RandomGenerator.getDefault().nextInt(0, 3))
                .tour(TourResponse.from(itinerary.getTour()))
                .missions(itinerary.getItineraryMissionMappings()
                        .stream()
                        .map(ItineraryMissionMappingResponse::from)
                        .toList())
                .build();
    }

    public static CurrentItineraryResponse empty() {
        return CurrentItineraryResponse.builder()
                .id(null)
                .startDate(null)
                .endDate(null)
                .remainingDay(0)
                .tour(null)
                .missions(null)
                .build();
    }

    @Getter
    @Builder
    public static class ItineraryMissionMappingResponse {

        private String name;
        private String description;
        private Boolean isComplete;

        public static ItineraryMissionMappingResponse from(ItineraryMissionMapping itineraryMissionMapping) {
            if (itineraryMissionMapping == null) {
                return null;
            }

            return ItineraryMissionMappingResponse.builder()
                    .isComplete(itineraryMissionMapping.getStatus() == MissionStatus.COMPLETE)
                    .name(itineraryMissionMapping.getMission().getName())
                    .description(itineraryMissionMapping.getMission().getDescription())
                    .build();
        }

    }


}

