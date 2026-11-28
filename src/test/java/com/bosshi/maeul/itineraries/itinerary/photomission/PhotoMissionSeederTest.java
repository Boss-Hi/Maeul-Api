package com.bosshi.maeul.itineraries.itinerary.photomission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("PhotoMissionSeeder 기준 사진")
class PhotoMissionSeederTest {

    private final PhotoMissionPlaceRepository placeRepository = mock(PhotoMissionPlaceRepository.class);
    private final PhotoMissionReferencePhotoRepository photoRepository = mock(PhotoMissionReferencePhotoRepository.class);
    private final PhotoMissionSeeder seeder = new PhotoMissionSeeder(placeRepository, photoRepository);

    private void withPlace() {
        when(placeRepository.findByEnabledTrueAndDeletedAtIsNull()).thenReturn(List.of(
                PhotoMissionPlace.builder().id(1L).keyword("융건릉").name("정조효공원·융건릉").enabled(true).build()));
    }

    @Test
    @DisplayName("3장을 코드(base64)와 함께 만들고, 실제 이미지 파일에서 읽은 JPEG 이다")
    void createsThreePhotosFromImageFiles() throws Exception {
        withPlace();
        when(photoRepository.existsByCode(anyString())).thenReturn(false);

        seeder.run();

        ArgumentCaptor<PhotoMissionReferencePhoto> saved = ArgumentCaptor.forClass(PhotoMissionReferencePhoto.class);
        verify(photoRepository, times(3)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(PhotoMissionReferencePhoto::getCode)
                .containsExactly("YUNGNEUNG-1", "YUNGNEUNG-2", "YUNGNEUNG-3");
        assertThat(saved.getAllValues()).allSatisfy(photo -> {
            assertThat(photo.getMimeType()).isEqualTo("image/jpeg");
            assertThat(photo.isEnabled()).isTrue();
            assertThat(PhotoMissionRules.detectMime(Base64.getDecoder().decode(photo.getImageBase64()))).isEqualTo("image/jpeg");
        });
    }

    @Test
    @DisplayName("이미 있는 코드는 다시 만들지 않고, 장소가 없으면 아무것도 만들지 않는다")
    void skipsExistingCodesAndMissingPlace() throws Exception {
        withPlace();
        when(photoRepository.existsByCode("YUNGNEUNG-1")).thenReturn(true);
        when(photoRepository.existsByCode("YUNGNEUNG-2")).thenReturn(true);
        when(photoRepository.existsByCode("YUNGNEUNG-3")).thenReturn(true);
        seeder.run();
        verify(photoRepository, never()).save(any());

        when(placeRepository.findByEnabledTrueAndDeletedAtIsNull()).thenReturn(List.of());
        seeder.run();
        verify(photoRepository, never()).save(any());
    }
}
