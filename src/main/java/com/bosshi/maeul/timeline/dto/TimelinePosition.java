package com.bosshi.maeul.timeline.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimelinePosition(
        double latitude,
        double longitude,
        LocalDateTime recordedAt
) {
}
