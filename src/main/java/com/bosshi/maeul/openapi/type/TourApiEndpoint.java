package com.bosshi.maeul.openapi.type;

import lombok.Getter;

@Getter
public enum TourApiEndpoint {
    LOCATION_BASED_LIST("locationBasedList2"),
    SEARCH_KEYWORD("searchKeyword2"),
    SEARCH_FESTIVAL("searchFestival2"),
    SEARCH_STAY("searchStay2"),
    DETAIL_COMMON("detailCommon2"),
    DETAIL_INTRO("detailIntro2"),
    DETAIL_INFO("detailInfo2"),
    DETAIL_IMAGE("detailImage2"),
    AREA_BASED_SYNC_LIST("areaBasedSyncList2"),
    AREA_CODE("areaCode2"),
    DETAIL_PET_TOUR("detailPetTour2"),
    CATEGORY_CODE("categoryCode2"),
    AREA_BASED_LIST("areaBasedList2"),
    LDONG_CODE("ldongCode2"),
    LCLS_SYSTEM_CODE("lclsSystmCode2");

    private final String path;

    TourApiEndpoint(String path) {
        this.path = path;
    }

}
