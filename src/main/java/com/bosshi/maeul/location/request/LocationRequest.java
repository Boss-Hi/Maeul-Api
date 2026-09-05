package com.bosshi.maeul.location.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationRequest {

    @NotNull(message = "위도는 필수입니다.")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다.")
    @Max(value = 90, message = "위도는 90 이하이어야 합니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다.")
    @Max(value = 180, message = "경도는 180 이하이어야 합니다.")
    private Double longitude;

    private Double accuracy; // 선택적 매개변수
}