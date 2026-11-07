package com.bosshi.maeul.timeline.response;

import java.time.LocalDate;

public record TimelineDeleteResponse(LocalDate date, int deleted) {
}
