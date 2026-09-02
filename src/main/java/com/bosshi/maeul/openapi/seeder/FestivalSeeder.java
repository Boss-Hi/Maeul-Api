package com.bosshi.maeul.openapi.seeder;

import com.bosshi.maeul.openapi.entity.Festival;
import com.bosshi.maeul.openapi.repository.FestivalRepository;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.service.OpenApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FestivalSeeder implements CommandLineRunner {

    private final FestivalRepository repository;
    private final OpenApiService openApiService;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        List<Festival> festivals = openApiService.searchFestival(
                        SearchFestivalRequest.builder()
                                .numOfRows(1000)
                                .lclsSystm1("EV")
                                .eventStartDate("20260901")
                                .build()
                )
                .getResponse()
                .getBody()
                .getItems()
                .getItem()
                .stream()
                .map(item -> Festival.builder()
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
                        .mapX(Double.valueOf(item.getMapx()))
                        .mapY(Double.valueOf(item.getMapy()))
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
    }
}
