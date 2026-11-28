package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.ai.AiGenerator;
import com.bosshi.maeul.ai.AiImage;
import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.AiResult;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** AI 로 기준 사진과 올린 사진이 같은 장소인지 판정한다. AI 가 답하지 못하면 통과시키지 않고 실패로 알린다 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoJudge {

    static final String JSON_EXAMPLE = """
            {"same": true, "confidence": 0.9, "matchedIndex": 2, "reason": "판단 사유"}""";

    private final AiGenerator aiGenerator;

    record Verdict(String provider, PhotoJudgement judgement) {
    }

    Verdict judge(List<AiImage> references, AiImage upload) {
        List<AiImage> images = new ArrayList<>(references);
        images.add(upload);
        AiRequest<PhotoJudgement> request = AiRequest.withImages(prompt(references.size()), images, PhotoJudgement.class, JSON_EXAMPLE);
        try {
            AiResult<PhotoJudgement> result = aiGenerator.generate(request);
            if (result.value() == null) {
                throw new IllegalStateException(result.provider() + " 응답이 비어 있습니다.");
            }
            return new Verdict(result.provider(), result.value());
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException(PhotoMissionRules.UNAVAILABLE_MESSAGE, e);
        } catch (RuntimeException e) {
            log.error("사진 미션 AI 판정에 실패했습니다.", e);
            throw new ServiceUnavailableException(PhotoMissionRules.UNAVAILABLE_MESSAGE, e);
        }
    }

    static String prompt(int referenceCount) {
        return """
                앞의 %d장은 미션의 기준 사진이고, 마지막 1장은 사용자가 방금 그 장소에서 촬영해 올린 사진입니다.
                기준 사진들은 같은 장소 안의 서로 다른 지점입니다.
                올린 사진이 기준 사진 중 하나라도 같은 지점(같은 건물, 구조물, 조형물)을 찍은 것인지 판단하세요.
                - 촬영 각도, 거리, 조명, 계절, 날씨가 달라도 같은 지점이면 same=true 이고, matchedIndex 에 그 기준 사진의 번호(1부터)를 쓰세요.
                - 올린 사진이 모니터나 휴대폰 화면을 다시 찍은 것, 인터넷 이미지를 캡처한 것, 기준 사진을 그대로 옮긴 것이면 same=false 입니다.
                - 어느 기준 사진과도 맞지 않거나 장소를 알아볼 수 없으면 same=false 이고 matchedIndex 는 0 입니다.
                - confidence 는 같은 지점이라고 확신하는 정도를 0 에서 1 사이 숫자로 쓰세요.
                - reason 은 한 문장으로 짧게 쓰세요.
                """.formatted(referenceCount);
    }
}
