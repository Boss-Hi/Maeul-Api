package com.bosshi.maeul.itinerary.filter;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.entity.TourCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TourFilterContext {
    private final Tour tour;
    private final ItineraryGenerateDTO.FilterSettingsRequest filterSettings;
    // 기준이 되는 메인 투어 위치 및 필터 정보
    private final List<TourCategory> selectedCategories;
    // 파이프라인을 거치며 정형화/필터링되는 투어 목록
    private List<Tour> candidateTours;
}