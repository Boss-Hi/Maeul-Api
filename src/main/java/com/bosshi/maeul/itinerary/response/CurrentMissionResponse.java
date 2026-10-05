package com.bosshi.maeul.itinerary.response;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryMissionMapping;
import com.bosshi.maeul.itinerary.entity.ItineraryMissionTaskMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.random.RandomGenerator;

@Getter
@Builder
public class CurrentMissionResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String name;
    private String description;
    private int totalCount;
    private int completedCount;
    private int percentage;
    private int remainingDay;
    private int neighborhoodCount;

    private List<ItineraryMissionTaskMappingResponse> missionTasks;

    public static CurrentMissionResponse from(Itinerary itinerary) {
        if (itinerary == null) {
            return null;
        }

        ItineraryMissionMapping itineraryMissionMapping = itinerary.getItineraryMissionMappings() != null && !itinerary.getItineraryMissionMappings().isEmpty()
                ? itinerary.getItineraryMissionMappings().getFirst()
                : null;

        if (itineraryMissionMapping == null) {
            return null;
        }

        int totalCount = itineraryMissionMapping.getItineraryMissionTaskMappings().size();
        int completedCount = (int) itineraryMissionMapping.getItineraryMissionTaskMappings().stream().filter(ItineraryMissionTaskMapping::getIsComplete).count();

        return CurrentMissionResponse.builder()
                .id(itinerary.getId())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .remainingDay((int) (itinerary.getEndDate().toEpochDay() - LocalDate.now().toEpochDay()))
                // @Todo: 임시로 neighborhoodCount를 랜덤으로 설정, 추후 실제 데이터로 변경 필요
                .neighborhoodCount(RandomGenerator.getDefault().nextInt(0, 3))
                .missionTasks(itineraryMissionMapping.getItineraryMissionTaskMappings().stream()
                .map(ItineraryMissionTaskMappingResponse::from)
                .toList())
                .name(itineraryMissionMapping.getMission().getName())
                .description(itineraryMissionMapping.getMission().getDescription())
                .totalCount(totalCount)
                .completedCount(completedCount)
                .percentage(totalCount == 0 ? 0 : (int) ((double) completedCount / totalCount * 100))
                .build();
    }

    @Getter
    @Builder
    public static class ItineraryMissionTaskMappingResponse {

        private String name;
        private String description;
        private Boolean isComplete;

        public static ItineraryMissionTaskMappingResponse from(ItineraryMissionTaskMapping itineraryMissionTaskMapping) {
            if (itineraryMissionTaskMapping == null) {
                return null;
            }

            return ItineraryMissionTaskMappingResponse.builder()
                    .isComplete(itineraryMissionTaskMapping.getIsComplete())
                    .name(itineraryMissionTaskMapping.getName())
                    .description(itineraryMissionTaskMapping.getDescription())
                    .build();
        }

    }


}

