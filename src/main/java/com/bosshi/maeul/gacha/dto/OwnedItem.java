package com.bosshi.maeul.gacha.dto;

/** 사용자가 가진 아이템과 수량 */
public record OwnedItem(Long itemId, String name, String imageUrl, int quantity) {
}
