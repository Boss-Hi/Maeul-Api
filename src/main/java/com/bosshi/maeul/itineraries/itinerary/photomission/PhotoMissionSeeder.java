package com.bosshi.maeul.itineraries.itinerary.photomission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * 정조효공원·융건릉 사진 미션의 기준 사진 3장을 코드(base64)로 만들어 둔다.
 * 사진 3장 중 하나라도 맞으면 성공이다 (ADR 0017). 코드가 이미 있으면 다시 만들지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(20)
public class PhotoMissionSeeder implements CommandLineRunner {

    static final String PLACE_KEYWORD = "융건릉";

    /** 코드와 classpath 의 이미지 파일 */
    record Seed(String code, String resource) {
    }

    static final List<Seed> SEEDS = List.of(
            new Seed("YUNGNEUNG-1", "photo-mission/yungneung-1.jpg"),
            new Seed("YUNGNEUNG-2", "photo-mission/yungneung-2.jpg"),
            new Seed("YUNGNEUNG-3", "photo-mission/yungneung-3.jpg"));

    private final PhotoMissionPlaceRepository placeRepository;
    private final PhotoMissionReferencePhotoRepository photoRepository;

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        PhotoMissionPlace place = placeRepository.findByEnabledTrueAndDeletedAtIsNull().stream()
                .filter(p -> PLACE_KEYWORD.equals(p.getKeyword()))
                .findFirst()
                .orElse(null);
        if (place == null) {
            log.warn("[PhotoMissionSeeder] 장소 '{}' 가 없어 기준 사진을 만들지 않습니다.", PLACE_KEYWORD);
            return;
        }
        for (Seed seed : SEEDS) {
            if (photoRepository.existsByCode(seed.code())) {
                continue;
            }
            byte[] data = new ClassPathResource(seed.resource()).getContentAsByteArray();
            String mime = PhotoMissionRules.detectMime(data);
            if (mime == null) {
                throw new IllegalStateException("기준 사진이 JPEG, PNG, WebP 가 아닙니다: " + seed.resource());
            }
            photoRepository.save(PhotoMissionReferencePhoto.builder()
                    .placeId(place.getId())
                    .code(seed.code())
                    .mimeType(mime)
                    .imageBase64(Base64.getEncoder().encodeToString(data))
                    .enabled(true)
                    .build());
            log.info("[PhotoMissionSeeder] 기준 사진 생성: {}", seed.code());
        }
    }
}
