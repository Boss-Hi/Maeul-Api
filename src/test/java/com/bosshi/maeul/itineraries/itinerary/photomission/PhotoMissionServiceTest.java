package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.ai.AiImage;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.gacha.GachaTicketIssuer;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.itineraries.itinerary.event.MissionCompletedEvent;
import com.bosshi.maeul.itineraries.itinerary.repository.ItineraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("PhotoMissionService 사진 미션")
class PhotoMissionServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);

    private final PhotoMissionRepository missionRepository = mock(PhotoMissionRepository.class);
    private final PhotoMissionPlaceRepository placeRepository = mock(PhotoMissionPlaceRepository.class);
    private final PhotoMissionReferencePhotoRepository referenceRepository = mock(PhotoMissionReferencePhotoRepository.class);
    private final PhotoMissionAttemptRepository attemptRepository = mock(PhotoMissionAttemptRepository.class);
    private final ReferenceImageFetcher fetcher = mock(ReferenceImageFetcher.class);
    private final PhotoJudge judge = mock(PhotoJudge.class);
    private final GachaTicketIssuer ticketIssuer = mock(GachaTicketIssuer.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final PhotoMissionService service = new PhotoMissionService(missionRepository, placeRepository, referenceRepository,
            attemptRepository, mock(ItineraryRepository.class), fetcher, judge, ticketIssuer, publisher,
            Clock.fixed(TODAY.atTime(12, 0).atZone(ZONE).toInstant(), ZONE), new Random(1), 0.7, 5, Duration.ofMinutes(30), 5 * 1024 * 1024);

    private static byte[] jpeg(int seed) throws Exception {
        BufferedImage image = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        image.setRGB(seed % 40, seed % 30, 0xFFFFFF);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    private static PhotoMission mission(PhotoMissionStatus status) {
        return PhotoMission.builder().id(7L).itineraryId(1L).userId(3L).placeId(2L).status(status)
                .regionCode("41").subdivisionCode("590").startDate(TODAY).endDate(TODAY.plusDays(1)).build();
    }

    @BeforeEach
    void setUp() throws Exception {
        when(missionRepository.findById(7L)).thenReturn(Optional.of(mission(PhotoMissionStatus.READY)));
        when(placeRepository.findById(2L)).thenReturn(Optional.of(PhotoMissionPlace.builder().id(2L).keyword("융건릉").name("융건릉")
                .latitude(37.2081).longitude(126.9892).radiusM(150).enabled(true).build()));
        String base64 = Base64.getEncoder().encodeToString(jpeg(1));
        when(referenceRepository.findByPlaceIdAndEnabledTrueAndDeletedAtIsNullOrderByIdAsc(2L)).thenReturn(List.of(
                reference(11L, base64), reference(12L, base64), reference(13L, base64)));
        when(attemptRepository.existsByUserIdAndImageHash(anyLong(), anyString())).thenReturn(false);
        when(attemptRepository.countByPhotoMissionIdAndAttemptedOn(7L, TODAY)).thenReturn(0L);
        when(missionRepository.markComplete(anyLong(), any(), anyBoolean())).thenReturn(1);
        when(missionRepository.existsRewardedOverlap(any(), any(), any(), any(), any(), anyLong())).thenReturn(false);
    }

    private static PhotoMissionReferencePhoto reference(Long id, String base64) {
        return PhotoMissionReferencePhoto.builder().id(id).placeId(2L).code("YUNGNEUNG-" + id).mimeType("image/jpeg")
                .imageBase64(base64).enabled(true).build();
    }

    private void judged(boolean same, double confidence) {
        when(judge.judge(anyList(), any())).thenReturn(new PhotoJudge.Verdict("claude", new PhotoJudgement(same, confidence, same ? 2 : 0, "r")));
    }

    @Test
    @DisplayName("같은 장소로 확신하면 미션을 완료하고 가챠권 1장과 미션 완료 이벤트를 준다")
    void successIssuesTicketAndEvent() throws Exception {
        judged(true, 0.9);

        PhotoMissionService.SubmitResult result = service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892);

        assertThat(result.success()).isTrue();
        assertThat(result.ticketIssued()).isTrue();
        verify(ticketIssuer).issue(3L, TicketEventType.PHOTO_MISSION, "photo-mission:7");
        verify(publisher).publishEvent(new MissionCompletedEvent(3L, PhotoMissionService.EVENT_ID_OFFSET + 7L, "41", "590"));
    }

    @Test
    @DisplayName("기준 사진 3장과 올린 사진 1장을 한 번에 AI 로 보내고, 하나라도 맞으면 성공이다")
    void sendsAllReferencePhotosAndSucceedsOnAnyMatch() throws Exception {
        judged(true, 0.9);

        PhotoMissionService.SubmitResult result = service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892);

        org.mockito.ArgumentCaptor<List<AiImage>> references = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(judge).judge(references.capture(), any(AiImage.class));
        assertThat(references.getValue()).hasSize(3);
        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("기준 사진이 하나도 없으면 AI 를 부르지 않고 사용할 수 없음으로 알린다")
    void noReferencePhotosIsUnavailable() throws Exception {
        when(referenceRepository.findByPlaceIdAndEnabledTrueAndDeletedAtIsNullOrderByIdAsc(2L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).isInstanceOf(ServiceUnavailableException.class);

        verifyNoInteractions(judge);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("같은 시군구에 기간이 겹치고 이미 보상받은 사진 미션이 있으면 완료는 되지만 가챠권은 주지 않는다")
    void overlapSkipsTicket() throws Exception {
        judged(true, 0.9);
        when(missionRepository.existsRewardedOverlap(any(), any(), any(), any(), any(), anyLong())).thenReturn(true);

        PhotoMissionService.SubmitResult result = service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892);

        assertThat(result.success()).isTrue();
        assertThat(result.ticketIssued()).isFalse();
        verify(ticketIssuer, never()).issue(any(), any(), any());
    }

    @Test
    @DisplayName("다른 장소이거나 확신도가 기준 미만이면 실패로 기록하고 남은 횟수를 돌려준다")
    void notSameOrLowConfidenceFails() throws Exception {
        judged(false, 0.95);
        PhotoMissionService.SubmitResult notSame = service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892);
        judged(true, 0.69);
        PhotoMissionService.SubmitResult low = service.submit(3L, 7L, jpeg(3), 37.2081, 126.9892);

        assertThat(notSame.result()).isEqualTo(AttemptResult.NOT_SAME);
        assertThat(low.result()).isEqualTo(AttemptResult.LOW_CONFIDENCE);
        assertThat(notSame.remainingAttempts()).isEqualTo(4);
        verify(missionRepository, never()).markComplete(anyLong(), any(), anyBoolean());
        verify(ticketIssuer, never()).issue(any(), any(), any());
    }

    @Test
    @DisplayName("장소 반경 밖, 일정 기간 밖, 이미 완료, 형식이 아닌 파일은 거절한다")
    void rejectsInvalidSubmissions() throws Exception {
        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.3081, 126.9892)).hasMessageContaining("근처");
        assertThatThrownBy(() -> service.submit(3L, 7L, "GIF89a______".getBytes(), 37.2081, 126.9892)).hasMessageContaining("JPEG");
        assertThatThrownBy(() -> service.submit(4L, 7L, jpeg(2), 37.2081, 126.9892)).hasMessageContaining("찾을 수 없습니다");
        PhotoMission expired = mission(PhotoMissionStatus.READY);
        expired.setEndDate(TODAY.minusDays(1));
        expired.setStartDate(TODAY.minusDays(2));
        when(missionRepository.findById(7L)).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).hasMessageContaining("일정 기간");
        when(missionRepository.findById(7L)).thenReturn(Optional.of(mission(PhotoMissionStatus.COMPLETE)));
        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).hasMessageContaining("이미 완료");
        verifyNoInteractions(judge);
    }

    @Test
    @DisplayName("하루 시도 한도(5회)를 넘으면 거절하고, 같은 사진은 다시 받지 않는다")
    void rejectsOverLimitAndDuplicatePhoto() throws Exception {
        when(attemptRepository.countByPhotoMissionIdAndAttemptedOn(7L, TODAY)).thenReturn(5L);
        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).hasMessageContaining("시도 횟수");

        when(attemptRepository.countByPhotoMissionIdAndAttemptedOn(7L, TODAY)).thenReturn(0L);
        when(attemptRepository.existsByUserIdAndImageHash(anyLong(), anyString())).thenReturn(true);
        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).hasMessageContaining("이미 올린 사진");
        verifyNoInteractions(judge);
    }

    @Test
    @DisplayName("AI 가 답하지 못하면 통과시키지 않고 시도로 세지도 않는다")
    void aiFailureIsNotAnAttempt() throws Exception {
        when(judge.judge(anyList(), any())).thenThrow(new ServiceUnavailableException("x"));

        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).isInstanceOf(ServiceUnavailableException.class);

        verify(attemptRepository, never()).save(any());
        verify(missionRepository, never()).markComplete(anyLong(), any(), anyBoolean());
    }

    @Test
    @DisplayName("동시에 성공해 이미 완료 처리된 경우 가챠권을 발급하지 않는다")
    void concurrentCompletionIssuesNothing() throws Exception {
        judged(true, 0.9);
        when(missionRepository.markComplete(anyLong(), any(), anyBoolean())).thenReturn(0);

        assertThatThrownBy(() -> service.submit(3L, 7L, jpeg(2), 37.2081, 126.9892)).hasMessageContaining("이미 완료");

        verify(ticketIssuer, never()).issue(any(), any(), any());
        verify(publisher, never()).publishEvent(any(MissionCompletedEvent.class));
    }
}
