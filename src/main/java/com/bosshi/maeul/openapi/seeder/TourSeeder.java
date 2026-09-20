package com.bosshi.maeul.openapi.seeder;

import com.bosshi.maeul.openapi.dto.AreaBasedSyncListDTO;
import com.bosshi.maeul.openapi.dto.SearchFestivalDTO;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import com.bosshi.maeul.openapi.service.OpenApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Order(3)
public class TourSeeder implements CommandLineRunner {

    private final TourRepository repository;
    private final OpenApiService openApiService;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        List<Tour> festivals = openApiService.searchFestival(
                        SearchFestivalDTO.builder()
                                .eventStartDate("20260101")
                                .numOfRows(5000)
                                .build()
                )
                .getResponse()
                .getBody()
                .getItems()
                .getItem()
                .stream()
                .map(item -> Tour.builder()
                        .addr1(item.getAddr1())
                        .addr2(item.getAddr2())
                        .zipcode(item.getAreacode())
                        .cat1(item.getCat1())
                        .cat2(item.getCat2())
                        .cat3(item.getCat3())
                        .contentId(item.getContentid())
                        .contentTypeId(item.getContenttypeid())
                        .createdTime(item.getCreatedtime())
                        .eventStartDate(item.getEventstartdate())
                        .eventEndDate(item.getEventenddate())
                        .firstImage(item.getFirstimage())
                        .firstImage2(item.getFirstimage2())
                        .mapX(parseDoubleSafely(item.getMapx()))
                        .mapY(parseDoubleSafely(item.getMapy()))
                        .mLevel(item.getMlevel())
                        .modifiedTime(item.getModifiedtime())
                        .areaCode(item.getAreacode())
                        .sigunguCode(item.getSigungucode())
                        .tel(item.getTel())
                        .title(item.getTitle())
                        .lDongRegnCd(item.getLDongRegnCd())
                        .lDongSignguCd(item.getLDongSignguCd())
                        .lclsSystm1(item.getLclsSystm1())
                        .lclsSystm2(item.getLclsSystm2())
                        .lclsSystm3(item.getLclsSystm3())
                        .progressType(item.getProgresstype())
                        .festivalType(item.getFestivaltype())
                        .build())
                .toList();
        repository.saveAll(festivals);

        // 이미 등록된 축제의 contentId 목록을 Set으로 추출[cite: 1]
        Set<String> festivalContentIds = festivals.stream()
                .map(Tour::getContentId)
                .collect(Collectors.toSet());

        List<Tour> tours = openApiService.getAreaBasedSyncList(
                        AreaBasedSyncListDTO.builder()
                                .numOfRows(10000)
                                .build()
                )
                .getResponse()
                .getBody()
                .getItems()
                .getItem()
                .stream()
                // 축제 목록에 이미 존재하는 contentId는 제외 처리[cite: 1]
                .filter(item -> !festivalContentIds.contains(item.getContentid()))
                .map(item -> Tour.builder()
                        .addr1(item.getAddr1())
                        .addr2(item.getAddr2())
                        .zipcode(item.getAreacode())
                        .cat1(item.getCat1())
                        .cat2(item.getCat2())
                        .cat3(item.getCat3())
                        .contentId(item.getContentid())
                        .contentTypeId(item.getContenttypeid())
                        .createdTime(item.getCreatedtime())
                        .firstImage(item.getFirstimage())
                        .firstImage2(item.getFirstimage2())
                        .mapX(parseDoubleSafely(item.getMapx()))
                        .mapY(parseDoubleSafely(item.getMapy()))
                        .mLevel(item.getMlevel())
                        .modifiedTime(item.getModifiedtime())
                        .areaCode(item.getAreacode())
                        .sigunguCode(item.getSigungucode())
                        .tel(item.getTel())
                        .title(item.getTitle())
                        .lDongRegnCd(item.getLDongRegnCd())
                        .lDongSignguCd(item.getLDongSignguCd())
                        .lclsSystm1(item.getLclsSystm1())
                        .lclsSystm2(item.getLclsSystm2())
                        .lclsSystm3(item.getLclsSystm3())
                        .progressType(item.getProgresstype())
                        .festivalType(item.getFestivaltype())
                        .build())
                .toList();
        repository.saveAll(tours);
    }

    private Double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null; // 또는 기본값 0.0D
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null; // 숫자로 변환 불가능한 값 예외 처리
        }
    }
}
