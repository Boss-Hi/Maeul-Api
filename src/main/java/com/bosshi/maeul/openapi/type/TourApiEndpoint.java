package com.bosshi.maeul.openapi.type;

import lombok.Getter;

@Getter
public enum TourApiEndpoint {
    LOCATION_BASED_LIST("/B551011/KorService2/locationBasedList2"),
    SEARCH_KEYWORD("/B551011/KorService2/searchKeyword2"),
    SEARCH_FESTIVAL("/B551011/KorService2/searchFestival2"),
    SEARCH_STAY("/B551011/KorService2/searchStay2"),
    DETAIL_COMMON("/B551011/KorService2/detailCommon2"),
    DETAIL_INTRO("/B551011/KorService2/detailIntro2"),
    DETAIL_INFO("/B551011/KorService2/detailInfo2"),
    DETAIL_IMAGE("/B551011/KorService2/detailImage2"),
    AREA_BASED_SYNC_LIST("/B551011/KorService2/areaBasedSyncList2"),
    AREA_CODE("/B551011/KorService2/areaCode2"),
    DETAIL_PET_TOUR("/B551011/KorService2/detailPetTour2"),
    CATEGORY_CODE("/B551011/KorService2/categoryCode2"),
    AREA_BASED_LIST("/B551011/KorService2/areaBasedList2"),
    LDONG_CODE("/B551011/KorService2/ldongCode2"),
    LCLS_SYSTEM_CODE("/B551011/KorService2/lclsSystmCode2"),

    AREA_TAR_SVC_DEM_LIST("/B551011/AreaTarResDemService/areaTarSvcDemList");

    private final String path;

    TourApiEndpoint(String path) {
        this.path = path;
    }
}
