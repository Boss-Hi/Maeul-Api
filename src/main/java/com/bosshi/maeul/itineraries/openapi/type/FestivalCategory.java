package com.bosshi.maeul.itineraries.openapi.type;

import lombok.Getter;

@Getter
public enum FestivalCategory {
    EV("EV"), EX("EX"), HS("HS"), VE("VE");

    private final String code;

    FestivalCategory(String code) {
        this.code = code;
    }
}
