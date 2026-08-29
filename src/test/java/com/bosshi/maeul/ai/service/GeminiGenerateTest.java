package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.request.GeminiGenerateRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GeminiGenerateTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    void generateTest() {
        GeminiGenerateResponse response = geminiService.generate(GeminiGenerateRequest.ofPrompt(
                        "제공된 장소 데이터(JSON)를 기반으로 지리적 위치(mapx, mapy)와 동선을 고려하여 최적의 여행 일정을 짜주세요.\n" +
                                "\n" +
                                "[응답 규칙]\n" +
                                "1. 응답은 마크다운 블록(```json 등)이나 설명 텍스트 없이 오직 순수한 JSON 객체 하나만 출력해야 합니다.\n" +
                                "2. 각 장소의 지형적 위치를 고려하여 이동 동선이 겹치지 않게 순서(order)를 배치하세요.\n" +
                                "3. 중복되거나 인접한 장소는 하나의 시간대에 묶거나 흐름에 맞게 배치하세요.\n" +
                                "\n" +
                                "[JSON 출력 스키마]\n" +
                                "  \"schedule\": [\n" +
                                "    {\n" +
                                "      \"day\": 1,\n" +
                                "      \"title\": \"일정 제목\",\n" +
                                "      \"route\": [\n" +
                                "        {\n" +
                                "          \"order\": 1,\n" +
                                "          \"contentid\": \"장소 contentid\",\n" +
                                "          \"title\": \"장소명\",\n" +
                                "          \"addr1\": \"주소\",\n" +
                                "          \"mapx\": \"경도\",\n" +
                                "          \"mapy\": \"위도\",\n" +
                                "          \"recommended_time\": \"추천 방문 시간\",\n" +
                                "          \"description\": \"방문 추천 이유 및 활동 설명\"\n" +
                                "        }\n" +
                                "      ]\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}\n" +
                                "\n" +
                                "[장소 데이터]\n" +
                                "{" +
                                "\"item\": [\n" +
                                "                        \"addr1\": \"부산광역 사하구 하단동 1195\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"2783344\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20211130184315\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/51/2787651_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/51/2787651_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type3\",\n" +
                                "                        \"mapx\": \"128.9491801483\",\n" +
                                "                        \"mapy\": \"35.1102108320\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260810152146\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"낙동강 생태탐방선\",\n" +
                                "                        \"zipcode\": \"604020\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA04\",\n" +
                                "                        \"lclsSystm3\": \"NA040500\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 몰운대1길 14 (다대동)\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"3027228\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20231027182816\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/94/3027194_image2_1.JPG\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/94/3027194_image3_1.JPG\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type3\",\n" +
                                "                        \"mapx\": \"128.9680205530435\",\n" +
                                "                        \"mapy\": \"35.046355897467016\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260420173053\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"다대포생태탐방로\",\n" +
                                "                        \"zipcode\": \"49527\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA04\",\n" +
                                "                        \"lclsSystm3\": \"NA040500\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 다대동\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"2614721\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20190809013341\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/30/3506330_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/30/3506330_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type1\",\n" +
                                "                        \"mapx\": \"128.9701\",\n" +
                                "                        \"mapy\": \"35.0405\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260312090840\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"몰운대(부산)\",\n" +
                                "                        \"zipcode\": \"49522\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA02\",\n" +
                                "                        \"lclsSystm3\": \"NA020900\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 낙동남로 1240\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"630874\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20081001235556\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/22/3495822_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/22/3495822_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type1\",\n" +
                                "                        \"mapx\": \"128.945866728608\",\n" +
                                "                        \"mapy\": \"35.1043892335363\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260311100340\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"낙동강하구에코센터\",\n" +
                                "                        \"zipcode\": \"49435\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA05\",\n" +
                                "                        \"lclsSystm3\": \"NA050100\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 몰운대1길 14\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"126079\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20031215090000\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/15/3497115_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/15/3497115_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type1\",\n" +
                                "                        \"mapx\": \"128.963151\",\n" +
                                "                        \"mapy\": \"35.046247\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260309150604\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"다대포해수욕장\",\n" +
                                "                        \"zipcode\": \"49527\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA02\",\n" +
                                "                        \"lclsSystm3\": \"NA020900\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 낙동남로 1240 (하단동)\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"127974\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20031208090000\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/21/3497121_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/21/3497121_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type1\",\n" +
                                "                        \"mapx\": \"128.94597747957368\",\n" +
                                "                        \"mapy\": \"35.10449270271807\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260309093631\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"을숙도 공원\",\n" +
                                "                        \"zipcode\": \"49435\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA04\",\n" +
                                "                        \"lclsSystm3\": \"NA040500\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 장림로93번길 72 (장림동)\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"2606204\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20190613003233\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/88/3493588_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/88/3493588_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type3\",\n" +
                                "                        \"mapx\": \"128.957464\",\n" +
                                "                        \"mapy\": \"35.080926\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260305134932\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"장림포구\",\n" +
                                "                        \"zipcode\": \"49478\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA02\",\n" +
                                "                        \"lclsSystm3\": \"NA020700\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 다대동\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"\",\n" +
                                "                        \"cat1\": \"\",\n" +
                                "                        \"cat2\": \"\",\n" +
                                "                        \"cat3\": \"\",\n" +
                                "                        \"contentid\": \"2784328\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20211201191924\",\n" +
                                "                        \"firstimage\": \"https://tong.visitkorea.or.kr/cms/resource/52/3492452_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"https://tong.visitkorea.or.kr/cms/resource/52/3492452_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type3\",\n" +
                                "                        \"mapx\": \"128.9839\",\n" +
                                "                        \"mapy\": \"35.0536\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20260305105912\",\n" +
                                "                        \"sigungucode\": \"\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"다대포항\",\n" +
                                "                        \"zipcode\": \"49526\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA02\",\n" +
                                "                        \"lclsSystm3\": \"NA020700\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 낙동남로 1240\",\n" +
                                "                        \"addr2\": \"(하단동)\",\n" +
                                "                        \"areacode\": \"6\",\n" +
                                "                        \"cat1\": \"A01\",\n" +
                                "                        \"cat2\": \"A0101\",\n" +
                                "                        \"cat3\": \"A01011800\",\n" +
                                "                        \"contentid\": \"2614714\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20190809012101\",\n" +
                                "                        \"firstimage\": \"http://tong.visitkorea.or.kr/cms/resource/37/3029337_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"http://tong.visitkorea.or.kr/cms/resource/37/3029337_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type1\",\n" +
                                "                        \"mapx\": \"128.9460085869\",\n" +
                                "                        \"mapy\": \"35.1045515050\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20250812133327\",\n" +
                                "                        \"sigungucode\": \"10\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"낙동강하구 (부산 국가지질공원)\",\n" +
                                "                        \"zipcode\": \"49435\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA02\",\n" +
                                "                        \"lclsSystm3\": \"NA020100\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"addr1\": \"부산광역시 사하구 하단동\",\n" +
                                "                        \"addr2\": \"\",\n" +
                                "                        \"areacode\": \"6\",\n" +
                                "                        \"cat1\": \"A01\",\n" +
                                "                        \"cat2\": \"A0101\",\n" +
                                "                        \"cat3\": \"A01010400\",\n" +
                                "                        \"contentid\": \"2674974\",\n" +
                                "                        \"contenttypeid\": \"12\",\n" +
                                "                        \"createdtime\": \"20201007045450\",\n" +
                                "                        \"firstimage\": \"http://tong.visitkorea.or.kr/cms/resource/46/3029246_image2_1.jpg\",\n" +
                                "                        \"firstimage2\": \"http://tong.visitkorea.or.kr/cms/resource/46/3029246_image3_1.jpg\",\n" +
                                "                        \"cpyrhtDivCd\": \"Type1\",\n" +
                                "                        \"mapx\": \"128.9851266839\",\n" +
                                "                        \"mapy\": \"35.1310381466\",\n" +
                                "                        \"mlevel\": \"6\",\n" +
                                "                        \"modifiedtime\": \"20250509170424\",\n" +
                                "                        \"sigungucode\": \"10\",\n" +
                                "                        \"tel\": \"\",\n" +
                                "                        \"title\": \"승학산\",\n" +
                                "                        \"zipcode\": \"49306\",\n" +
                                "                        \"lDongRegnCd\": \"26\",\n" +
                                "                        \"lDongSignguCd\": \"380\",\n" +
                                "                        \"lclsSystm1\": \"NA\",\n" +
                                "                        \"lclsSystm2\": \"NA01\",\n" +
                                "                        \"lclsSystm3\": \"NA010100\"\n" +
                                "                    }\n" +
                                "                ]\n" +
                                "}",
                        0.7,
                        5000
                )
        );

        System.out.println(response.getUsageMetadata());
        System.out.println(response);
        System.out.println(response.toString());

        assertThat("").isNotEmpty();
    }
}

