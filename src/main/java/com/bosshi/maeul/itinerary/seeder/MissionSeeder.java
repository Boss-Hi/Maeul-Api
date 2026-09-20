package com.bosshi.maeul.itinerary.seeder;

import com.bosshi.maeul.itinerary.entity.*;
import com.bosshi.maeul.itinerary.repository.MissionRepository;
import com.bosshi.maeul.itinerary.repository.MissionTriggerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(7)
public class MissionSeeder implements CommandLineRunner {

    private final MissionRepository missionRepository;
    private final MissionTriggerRepository missionTriggerRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (missionRepository.count() > 0) {
            return;
        }

        // 1. 기존에 저장된 Trigger 목록을 가져와서 Value를 키로 하는 Map 생성[cite: 14]
        Map<String, MissionTrigger> triggerMap = missionTriggerRepository.findAll()
                .stream()
                .collect(Collectors.toMap(MissionTrigger::getValue, trigger -> trigger));

        // 2. CSV 기반 데이터 선언 (미션ID, 미션명, 연관된 트리거 코드 묶음(쉼표 구분), 설명)
        // 실제 CSV 데이터의 내용을 탭(Tab) 또는 지정된 구분자로 복사/붙여넣기 하시면 됩니다.
        String rawCsvData = """
                미션ID	$   미션명	                $   트리거코드목록	                        $   상세설명                                                                                        $   미션테스크명
                FIRST	$   여행의 서막	            $   FIRST	                            $   앱 설치 후 최초로 아무 관광지든 반경 100m 진입 즉시(테마 무관, 전체 통틀어 최초 1건만 인정). 첫 여행 시 자동 매칭.   $    최초 방문
                AC-01	$   오늘 밤은 여기서! 체크인	    $   AC01,AC02,AC03,AC04,AC06            $   숙박시설 POI 반경 100m 진입 즉시. 체크인/체크아웃 시각 별도 확인 없음.                                      $    숙박시설 방문
                AC-02	$   텐트 치고 별 보기	        $   AC05	                            $   캠핑장 POI 반경 200m 진입 즉시. 캠핑장은 부지가 넓어 반경을 100m보다 크게(200m) 설정.                        $ 캠핑장 방문
                EV-01	$   축제 한복판 뛰어들기	        $   EV01	                            $   축제장 POI 반경 150m 진입 즉시. 축제장은 부지가 넓은 편이라 반경 150m 이내 진입 시점에 자동 인증.                $    축제장 방문
                EV-02	$   무대 앞 직관 성공	        $   EV02	                            $   공연장 POI 반경 80m 진입 즉시.                                                                     $ 공연장 방문
                EV-03	$   행사장 출첵!	            $   EV03	                            $   행사장 POI 반경 100m 진입 즉시.                                                                    $ 행사장 방문장 방문
                EX-01	$   손으로 만드는 여행	        $   EX01,EX02,EX03,EX04,EX07            $   체험장(전통/공예/농산어촌/산사 등) POI 반경 80m 진입 즉시.                                               $  체험장 방문
                EX-02	$   몸도 마음도 충전 타임	    $   EX05	                            $   웰니스시설 POI 반경 80m 진입 즉시.                                                                  $  웰니스시설 방문
                EX-03	$   공장 구경 탐정놀이	        $   EX06	                            $   산업관광시설 POI 반경 100m 진입 즉시.                                                                $  산업관광시설 방문
                FD-01	$   오늘의 한 끼 클리어	        $   FD01,FD02,FD03                      $   음식점 POI 반경 30m 진입 즉시. 오인증 방지를 위해 반경을 30m로 좁게 설정.                                   $    음식점 방문
                FD-02	$   한 잔의 여유	            $   FD04	                            $   주점 POI 반경 30m 진입 즉시.                                                                       $    주점 방문
                FD-03	$   카페인 충전 완료	        $   FD05	                            $   카페/찻집 POI 반경 30m 진입 즉시.                                                                   $ 카페/찻집 방문
                HS-01	$   타임슬립 도장깨기	        $   HS01,HS02	                        $   역사유적지/역사유물 POI 반경 100m 진입 즉시.                                                           $   역사유적지/역사유물 방문
                HS-02	$   고요한 발걸음	            $   HS03	                            $   종교성지 POI 반경 100m 진입 즉시.                                                                   $ 종교성지 방문
                HS-03	$   역사의 현장 그 자리에        $   HS04	                            $   안보관광지 POI 반경 150m 진입 즉시. 부지가 넓은 편이라 반경 150m로 설정.                                    $   안보관광지 방문
                LS-01	$   땅 위의 아드레날린	        $   LS01	                            $   육상레저시설 POI 반경 150m 진입 즉시.                                                                 $ 육상레저시설 방문
                LS-02	$   물 만난 물고기	            $   LS02	                            $   수상레저시설 POI 반경 150m 진입 즉시.                                                                 $ 수상레저시설 방문
                LS-03	$   하늘 위 짜릿함	            $   LS03	                            $   항공레저시설 POI 반경 300m 진입 즉시. 활주로·이착륙장 등 부지가 매우 넓어 반경 300m로 설정.                      $  항공레저시설 방문
                LS-04	$   다 해봐야 직성 풀림	    	$   LS04	                            $   복합레저시설 POI 반경 200m 진입 즉시.                                                                 $ 복합레저시설 방문
                LS-05	$   팀으로 뭉치면 더 신나!	    $   LS05	                            $   팀원 전원 체크 완료 시점. 팀원 각자 GPS로 반경 진입 인증 후 전원이 앱 내 함께 완료 버튼 클릭 시 집계.                $    팀 레저시설 방문
                NA-01	$   정상 정복 인증샷	        $   NA01	                            $   산 정상/거점 POI 반경 300m 진입 즉시.                                                                 $    산 정상/거점 방문
                NA-02	$   물멍 타임	                $   NA02	                            $   하천·해양 대표 지점 좌표 기준 반경 200m 진입 즉시.                                                        $   하천·해양 방문
                NA-03	$   자연 속으로 풍덩	        $   NA03,NA04,NA05	                    $   자연생태/공원 POI 반경 200m 진입 즉시.                                                                $ 자연생태/공원 방문
                SH-01	$   지름신 강림	            $   SH01,SH02,SH03,SH04,SH05,SH07	    $   쇼핑시설 POI 반경 50m 진입 즉시.                                                                     $    쇼핑시설 방문
                SH-02	$   시장 한 바퀴	            $   SH06	                            $   전통시장 POI 반경 100m 진입 즉시.                                                                    $    전통시장 방문
                VE-01	$   여기 왔다 인증샷	        $   VE01	                            $   랜드마크 POI 반경 50m 진입 즉시.                                                                     $    랜드마크 방문
                VE-02	$   동심으로 돌아가기	        $   VE02	                            $   테마공원 POI 반경 300m 진입 즉시.                                                                    $    테마공원 방문
                VE-03	$   잔디밭 산책	            $   VE03	                            $   도시공원 POI 반경 150m 진입 즉시.                                                                    $    도시공원 방문
                VE-04	$   골목골목 문화 탐방	        $   VE04	                            $   도시/지역문화관광 구역 반경 150m 진입 즉시.                                                              $  도시/지역문화관광 구역 방문
                VE-05	$   문화생활 만렙 찍기	        $   VE05,VE06,VE07,VE08,VE09,VE10,VE12	$   복합관광/공연/전시 등 문화시설 POI 반경 100m 진입 즉시.                                                    $   복합관광/공연/전시 등 문화시설 방문
                C01-02	$   느긋하게 걷기 완주	        $   C0114	                            $   힐링코스 시작 및 종료 지점 각각 반경 50m 진입 시 인증.                                                     $    힐링코스 방문
                C01-03	$   캠핑코스 정복	            $   C0116	                            $   캠핑코스 시작 및 종료 지점 각각 반경 50m 진입 시 인증.                                                     $    캠핑코스 방문 
                """;

        // 3. 텍스트 파싱 및 엔티티 생성
        List<Mission> missions = Arrays.stream(rawCsvData.split("\n"))
                .skip(1) // Header 라인 무시
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> {
                    String[] cols = line.split("\\$");
                    if (cols.length < 5) return null;

                    String missionCode = cols[0].trim();
                    String missionName = cols[1].trim();
                    String[] triggerCodes = cols[2].split(","); // "AC01,AC02" 등을 배열로 분리
                    String description = cols[3].trim();
                    String taskName = cols[4].trim();

                    // Mission 엔티티 생성
                    Mission mission = Mission.builder()
                            .code(missionCode)
                            .name(missionName)
                            .description(description)
                            .build();

                    // 미션당 하나의 MissionTask 생성 (mission 필드 연결)
                    MissionTask missionTask = MissionTask.builder()
                            .name(taskName)
                            .description(description)
                            .build();

                    // 각 트리거 코드에 해당하는 Trigger를 찾아 MissionTriggerMapping 으로 연결
                    for (String tCode : triggerCodes) {
                        String cleanCode = tCode.trim();
                        MissionTrigger trigger = triggerMap.get(cleanCode);

                        if (trigger != null) {
                            mission.getMissionTriggerMappings().add(MissionTriggerMapping.builder()
                                    .mission(mission)
                                    .missionTrigger(trigger)
                                    .build());
                        } else {
                            log.warn("[MissionSeeder] 트리거 코드 [{}]를 찾을 수 없어 미션 [{}]에 매핑하지 못했습니다.", cleanCode, missionCode);
                        }
                    }

                    mission.getMissionTaskMappings().add(MissionTaskMapping.builder()
                            .mission(mission)
                            .missionTask(missionTask)
                            .build());

                    return mission;
                })
                .filter(Objects::nonNull)
                .toList();

        // 4. 영속화
        if (!missions.isEmpty()) {
            missionRepository.saveAll(missions);
            log.info("[MissionSeeder] 총 {}개의 관광 미션(샘플) 데이터 적재 완료", missions.size());
        }
    }
}
