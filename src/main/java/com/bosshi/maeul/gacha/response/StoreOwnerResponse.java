package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.entity.StoreOwner;

public record StoreOwnerResponse(Long id, Long storeId, Long userId) {

    public static StoreOwnerResponse from(StoreOwner owner) {
        return new StoreOwnerResponse(owner.getId(), owner.getStoreId(), owner.getUserId());
    }
}
