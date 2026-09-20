package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.dto.DetailCommon2DTO;
import com.bosshi.maeul.openapi.dto.DetailIntro2DTO;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import com.bosshi.maeul.openapi.response.DetailCommonResponse;
import com.bosshi.maeul.openapi.response.DetailIntroResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourSyncService {
    private final OpenApiService openApiService;
    private final TourRepository tourRepository;

    /**
     * Open API를 호출하여 최신 데이터를 가져온 뒤, DB의 Tour 값과 다르면 업데이트합니다.
     */
    @Transactional
    public Tour syncDetailCommon(Tour tour) {
        if (tour == null || tour.getContentId() == null) {
            return tour;
        }

        DetailCommonResponse detailCommonResponse = openApiService.getDetailCommon2(
                DetailCommon2DTO.builder().contentId(tour.getContentId()).build()
        );

        // API 응답 데이터 추출 (Null Safe)
        DetailCommonResponse.Item commonItem = extractCommonItem(detailCommonResponse);

        // 값이 변경된 항목이 있다면 DB에 저장
        if (updateTourIfChanged(tour, commonItem, null)) {
            tourRepository.save(tour);
        }
        return tour;
    }

    @Transactional
    public Tour syncDetailIntro(Tour tour) {
        if (tour == null || tour.getContentId() == null) {
            return tour;
        }

        DetailIntroResponse detailIntroResponse = openApiService.getDetailIntro2(
                DetailIntro2DTO.builder().contentId(tour.getContentId()).contentTypeId(tour.getContentTypeId()).build()
        );

        // API 응답 데이터 추출 (Null Safe)
        DetailIntroResponse.Item introItem = extractIntroItem(detailIntroResponse);

        // 값이 변경된 항목이 있다면 DB에 저장
        if (updateTourIfChanged(tour, null, introItem)) {
            tourRepository.save(tour);
            log.info("Tour 데이터가 업데이트되었습니다. [ContentId: {}]", tour.getContentId());
        }
        return tour;
    }

    private DetailCommonResponse.Item extractCommonItem(DetailCommonResponse response) {
        if (response != null && response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null &&
                response.getResponse().getBody().getItems().getItem() != null &&
                !response.getResponse().getBody().getItems().getItem().isEmpty()) {
            return response.getResponse().getBody().getItems().getItem().get(0);
        }
        return null;
    }

    private DetailIntroResponse.Item extractIntroItem(DetailIntroResponse response) {
        if (response != null && response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null &&
                response.getResponse().getBody().getItems().getItem() != null &&
                !response.getResponse().getBody().getItems().getItem().isEmpty()) {
            return response.getResponse().getBody().getItems().getItem().get(0);
        }
        return null;
    }

    private boolean updateTourIfChanged(Tour tour, DetailCommonResponse.Item common, DetailIntroResponse.Item intro) {
        boolean isUpdated = false;

        // 1. 공통 정보(DetailCommon) 비교
        if (common != null) {
            if (common.getTitle() != null && !Objects.equals(tour.getTitle(), common.getTitle())) {
                tour.setTitle(common.getTitle());
                isUpdated = true;
            }
            if (common.getHomepage() != null && !Objects.equals(tour.getHomepage(), common.getHomepage())) {
                tour.setHomepage(common.getHomepage());
                isUpdated = true;
            }
            if (common.getOverview() != null && !Objects.equals(tour.getOverview(), common.getOverview())) {
                tour.setOverview(common.getOverview());
                isUpdated = true;
            }
            if (common.getTel() != null && !Objects.equals(tour.getTel(), common.getTel())) {
                tour.setTel(common.getTel());
                isUpdated = true;
            }
        }

        // 2. 소개 정보(DetailIntro) 비교
        if (intro != null) {
            if (intro.getPlaytime() != null && !Objects.equals(tour.getPlaytime(), intro.getPlaytime())) {
                tour.setPlaytime(intro.getPlaytime());
                isUpdated = true;
            }
            if (intro.getEventplace() != null && !Objects.equals(tour.getEventplace(), intro.getEventplace())) {
                tour.setEventplace(intro.getEventplace());
                isUpdated = true;
            }
            if (intro.getProgram() != null && !Objects.equals(tour.getProgram(), intro.getProgram())) {
                tour.setProgram(intro.getProgram());
                isUpdated = true;
            }
            if (intro.getEventstartdate() != null && !Objects.equals(tour.getEventStartDate(), intro.getEventstartdate())) {
                tour.setEventStartDate(intro.getEventstartdate());
                isUpdated = true;
            }
            if (intro.getEventenddate() != null && !Objects.equals(tour.getEventEndDate(), intro.getEventenddate())) {
                tour.setEventEndDate(intro.getEventenddate());
                isUpdated = true;
            }
            if (intro.getSponsor1() != null && !Objects.equals(tour.getSponsor1(), intro.getSponsor1())) {
                tour.setSponsor1(intro.getSponsor1());
                isUpdated = true;
            }
        }

        return isUpdated;
    }
}