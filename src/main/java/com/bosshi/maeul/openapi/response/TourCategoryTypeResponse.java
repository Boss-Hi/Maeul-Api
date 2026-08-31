package com.bosshi.maeul.openapi.response;

import com.bosshi.maeul.openapi.entity.TourCategoryType;

public record TourCategoryTypeResponse(
        Long id,
        String contentTypeId,
        String contentTypeIdMultiLang,
        String name
) {
    public static TourCategoryTypeResponse from(TourCategoryType type) {
        return new TourCategoryTypeResponse(
                type.getId(),
                type.getContentTypeId(),
                type.getContentTypeIdMultiLang(),
                type.getName()
        );
    }
}
