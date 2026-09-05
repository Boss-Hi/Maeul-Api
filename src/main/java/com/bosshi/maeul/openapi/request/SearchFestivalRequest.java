package com.bosshi.maeul.openapi.request;

import com.bosshi.maeul.location.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class SearchFestivalRequest {
    /**
     * 행사 시작일 (YYYYMMDD)
     */
    private String eventStartDate;
    /**
     * 행사 종료일 (YYYYMMDD)
     */
    private String eventEndDate;

    /**
     * 카테고리 Code
     */
    private String tourCategoryCode;

    /**
     * 위치 정보
     */
    private LocationDTO location;
}
