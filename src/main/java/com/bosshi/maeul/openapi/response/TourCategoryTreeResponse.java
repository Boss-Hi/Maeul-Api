package com.bosshi.maeul.openapi.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TourCategoryTreeResponse(
        String id,
        String name,
        List<ChildResponse> children
) {
    public static List<TourCategoryTreeResponse> buildTree(List<TourCategoryResponse> categories) {
        List<TourCategoryResponse> parents = categories.stream()
                .filter(c -> c.depth() == 1)
                .toList();

        Map<String, List<TourCategoryResponse>> childrenByParent = categories.stream()
                .filter(c -> c.depth() == 2)
                .collect(Collectors.groupingBy(TourCategoryResponse::parentCode));

        return parents.stream()
                .map(parent -> new TourCategoryTreeResponse(
                        parent.code().toLowerCase(),
                        parent.name(),
                        childrenByParent.getOrDefault(parent.code(), List.of()).stream()
                                .map(ChildResponse::from)
                                .toList()
                ))
                .toList();
    }

    public record ChildResponse(
            String id,
            String name
    ) {
        public static ChildResponse from(TourCategoryResponse category) {
            return new ChildResponse(
                    category.code().toLowerCase(),
                    category.name()
            );
        }
    }
}
