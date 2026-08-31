package com.bosshi.maeul.openapi.response;

import com.bosshi.maeul.openapi.entity.TourCategory;

public record TourCategoryResponse(
        Long id,
        String code,
        String name,
        Integer depth,
        String parentCode,
        String contentTypeId,
        String contentTypeIdMultiLang,
        String description
) {
    public static TourCategoryResponse from(TourCategory category) {
        return new TourCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDepth(),
                category.getParentCode(),
                category.getContentTypeId(),
                category.getContentTypeIdMultiLang(),
                category.getDescription()
        );
    }
}
