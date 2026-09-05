package com.bosshi.maeul.survey.seeder;

import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.entity.SurveyQuestion;
import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import com.bosshi.maeul.survey.repository.SurveyRepository;
import com.bosshi.maeul.survey.type.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(3)
public class SurveySeeder implements CommandLineRunner {

    private final SurveyRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        Survey survey = Survey.builder()
                .code("MAEUL_FIRST_SURVEY")
                .title("취향 조사")
                .description("취향에 대한 설문 입니다.")
                .build();

        List<SurveyQuestion> questions = List.of(
                createQuestion(survey, 1, "BASIC 01", "나이대를 알려주세요", "비슷한 체류 리듬과 관심사를 가진 일정을 먼저 추천해요.", QuestionType.SINGLE_CHOICE, List.of(
                        createOption(1, "10대", null, "age_10"),
                        createOption(2, "20대", null, "age_20"),
                        createOption(3, "30대", null, "age_30"),
                        createOption(4, "40대", null, "age_40"),
                        createOption(5, "50대", null, "age_50"),
                        createOption(6, "60대", null, "age_60")
                )),
                createQuestion(survey, 2, "BASIC 02", "요즘 어떤 일을 하나요?", "업무 가능 시간, 동행 추천, 워케이션 장소를 맞추는 데 사용해요.", QuestionType.SINGLE_CHOICE, List.of(
                        createOption(1, "직장인", "퇴근 후 일정과 주말 체류에 잘 맞아요.", "worker"),
                        createOption(2, "프리랜서", "작업 공간과 여유로운 동선을 함께 봐요.", "freelancer"),
                        createOption(3, "학생", "가성비와 활동성 높은 미션을 우선해요.", "student"),
                        createOption(4, "창업/자영업", "로컬 네트워킹과 영감 스팟을 섞어봐요.", "entrepreneur")
                )),
                createQuestion(survey, 3, "TRIP 03", "이번 체류의 목적은요?", "하나만 골라도 충분해요. MAEUL이 일정의 밀도를 조절할게요.", QuestionType.SINGLE_CHOICE, List.of(
                        createOption(1, "쉼과 회복", "느린 산책, 좋은 숙소, 조용한 카페 중심", "rest_recovery"),
                        createOption(2, "일과 여행", "코워킹, 콘센트 카페, 저녁 로컬 행사 중심", "work_travel"),
                        createOption(3, "새로운 경험", "공방, 공연, 액티비티, 동네 미션 중심", "new_experience"),
                        createOption(4, "사람 만나기", "팀 미션과 취향이 맞는 체류자 추천 중심", "meet_people")
                )),
                createQuestion(survey, 4, "TASTE 04", "끌리는 테마를 골라주세요", "여러 개를 골라도 좋아요. 추천 행사의 결이 달라져요.", QuestionType.MULTIPLE_CHOICE, List.of(
                        createOption(1, "커피", null, "coffee"),
                        createOption(2, "독서", null, "reading"),
                        createOption(3, "음악/공연", null, "music_performance"),
                        createOption(4, "로컬 맛집", null, "local_gourmet"),
                        createOption(5, "바다/자연", null, "sea_nature"),
                        createOption(6, "공방 체험", null, "craft_workshop"),
                        createOption(7, "역사 산책", null, "history_walk"),
                        createOption(8, "웰니스", null, "wellness")
                )),
                createQuestion(survey, 5, "STAY 05", "선호하는 숙소는요?", "이동 거리와 체류 감도를 같이 맞춰볼게요.", QuestionType.SINGLE_CHOICE, List.of(
                        createOption(1, "코리빙", "라운지, 커뮤니티, 워케이션 분위기", "coliving"),
                        createOption(2, "펜션/민박", "지역감 있는 조용한 체류", "pension_guesthouse"),
                        createOption(3, "호텔", "편의성과 안정적인 컨디션", "hotel"),
                        createOption(4, "게스트하우스", "가볍게 머물고 사람 만나기", "guesthouse")
                )),
                createQuestion(survey, 6, "TASTE 06", "식사는 어떤 쪽이 좋아요?", "추천 동선 안의 점심, 카페, 저녁 장소를 고를 때 반영해요.", QuestionType.MULTIPLE_CHOICE, List.of(
                        createOption(1, "한식", null, "korean_food"),
                        createOption(2, "로컬 노포", null, "local_classic"),
                        createOption(3, "카페/찻집", null, "cafe_tea"),
                        createOption(4, "시장 음식", null, "market_food"),
                        createOption(5, "가벼운 브런치", null, "light_brunch"),
                        createOption(6, "술집/펍", null, "pub_bar")
                )),
                createQuestion(survey, 7, "ROUTE 07", "일정 템포는요?", "마지막이에요. 이 선택으로 하루 일정 간격이 정해져요.", QuestionType.SINGLE_CHOICE, List.of(
                        createOption(1, "느긋하게", "하루 2-3곳, 머무는 시간이 긴 일정", "relaxed"),
                        createOption(2, "적당히 알차게", "하루 4-5곳, 미션과 쉼의 균형", "moderate"),
                        createOption(3, "촘촘하게", "하루 6곳 이상, 짧고 다양한 탐색", "packed")
                ))
        );

        survey.getQuestions().addAll(questions);
        repository.save(survey);
    }

    // Survey와 Question 간 연관관계 주입 헬퍼 메서드
    private SurveyQuestion createQuestion(Survey survey, int order, String badge, String title, String subTitle, QuestionType type, List<SurveyQuestionOption> options) {
        SurveyQuestion question = SurveyQuestion.builder()
                .survey(survey) // 부모 참조 지정
                .stepOrder(order)
                .stepBadge(badge)
                .title(title)
                .subTitle(subTitle)
                .questionType(type)
                .build();

        // 옵션들에도 question 부모 참조 지정
        options.forEach(opt -> opt.setQuestion(question)); // Entity에 setQuestion 또는 Builder 생성 시 넘김
        question.getOptions().addAll(options);

        return question;
    }

    // Question과 Option 간 헬퍼 메서드
    private SurveyQuestionOption createOption(int order, String label, String subLabel, String value) {
        return SurveyQuestionOption.builder()
                .optionOrder(order)
                .label(label)
                .subLabel(subLabel)
                .value(value)
                .build();
    }
}
