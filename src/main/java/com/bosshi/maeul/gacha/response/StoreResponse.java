package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.entity.Store;

public record StoreResponse(Long id, String name, String regionCode, String code) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getId(), store.getName(), store.getRegionCode(), store.getSubdivisionCode());
    }
}
