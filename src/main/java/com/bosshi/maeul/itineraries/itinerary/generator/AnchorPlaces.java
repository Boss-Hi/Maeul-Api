package com.bosshi.maeul.itineraries.itinerary.generator;

import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.bosshi.maeul.itineraries.openapi.entity.Tour;

import java.util.List;
import java.util.Set;

/**
 * 앵커 일정(주축제가 화성시인 일정)의 뼈대가 되는 앵커 장소 묶음.
 * 순서는 이동 동선 순서이며, 한국관광공사 Venue 가 아니므로 일정에는 NORMAL 항목으로 담긴다.
 */
final class AnchorPlaces {

    /** 시도 코드 (region_subdivisions.region_code, 경기도) */
    static final String HWASEONG_REGION_CODE = "41";
    /** 시군구 코드 (region_subdivisions.code): 화성시와 그 아래 구(만세구, 효행구, 병점구, 동탄구). 시도 코드와 이어 붙이지 않고 각각 비교한다 */
    static final Set<String> HWASEONG_CODES = Set.of("590", "591", "593", "595", "597");

    private static final String LODGING_CONTENT_TYPE_ID = "32";

    record AnchorPlace(String name, String keyword, String category, String role) {
    }

    static final List<AnchorPlace> HWASEONG = List.of(
            new AnchorPlace("동탄 호수공원·동탄역", "호수공원", "출발 거점", "출발 인증"),
            new AnchorPlace("정조효공원·융건릉", "융건릉", "역사 자원", "짧은 역사 미션(30분)"),
            new AnchorPlace("더온전히 정조효공원점", "더온전히", "로컬 상점", "첫 쿠폰 사용처(아메리카노)"),
            new AnchorPlace("전곡항·서해랑 케이블카", "전곡항", "앵커 시설", "오후 체험 미션(갯벌 체험 인증)"),
            new AnchorPlace("황금해안길(17km, 3구간)", "황금해안길", "앵커 시설", "저녁 낙조 걷기 = 체류 시간 쌓기"),
            new AnchorPlace("서해마루 유스호스텔", "서해마루", "숙박 거점", "1박 인증, 펫 성장 보너스"),
            new AnchorPlace("궁평항 에코팜랜드 캠핑장", "에코팜랜드", "숙박 거점", "캠핑 1박 인증, 야간 미션"),
            new AnchorPlace("궁평항 수산시장", "수산시장", "로컬 상점", "아침 식사 쿠폰, 특산물"));

    private AnchorPlaces() {
    }

    static boolean isAuxiliaryCandidate(Tour tour) {
        return tour.getAddr1() != null && tour.getAddr1().contains("화성시")
                && !LODGING_CONTENT_TYPE_ID.equals(tour.getContentTypeId());
    }

    static boolean hasRepeatedAnchorInDay(Itinerary itinerary) {
        return itinerary.getItineraryDays().stream().anyMatch(day -> HWASEONG.stream().anyMatch(place ->
                day.getItinerarySchedules().stream()
                        .filter(s -> s.getTitle() != null && s.getTitle().contains(place.keyword()))
                        .count() > 1));
    }

    static boolean isHwaseong(Tour tour) {
        return tour != null
                && HWASEONG_REGION_CODE.equals(tour.getLDongRegnCd())
                && HWASEONG_CODES.contains(tour.getLDongSignguCd());
    }
}
