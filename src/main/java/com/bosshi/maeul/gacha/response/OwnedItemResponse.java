package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.dto.OwnedItem;

public record OwnedItemResponse(Long itemId, String name, String imageUrl, int quantity) {

    public static OwnedItemResponse from(OwnedItem item) {
        return new OwnedItemResponse(item.itemId(), item.name(), item.imageUrl(), item.quantity());
    }
}
