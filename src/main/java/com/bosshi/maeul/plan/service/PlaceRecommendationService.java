package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.request.AreaBasedSyncListRequest;
import com.bosshi.maeul.openapi.service.OpenApiService;
import com.bosshi.maeul.plan.request.PlacePreferenceRequest;
import com.bosshi.maeul.plan.response.RecommendedPlace;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * areaBasedSyncList2 응답에서 취향 매칭 장소를 추출한다.
 */
@Service
public class PlaceRecommendationService {
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final OpenApiService openApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlaceRecommendationService(OpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    public List<RecommendedPlace> recommendPlaces(AreaBasedSyncListRequest request, PlacePreferenceRequest preference) {
        PlacePreferenceRequest effectivePreference = preference == null ? new PlacePreferenceRequest() : preference;
        String rawJson = openApiService.getAreaBasedSyncList(request);
        JsonNode itemsNode = extractItemsNode(rawJson);

        if (itemsNode == null || itemsNode.isMissingNode() || itemsNode.isNull()) {
            return List.of();
        }

        List<RankedPlace> rankedPlaces = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                rankedPlaces.add(toRankedPlace(item, effectivePreference));
            }
        } else if (itemsNode.isObject()) {
            rankedPlaces.add(toRankedPlace(itemsNode, effectivePreference));
        }

        boolean hasPreference = hasPreference(effectivePreference);
        int maxResults = resolveMaxResults(effectivePreference);

        return rankedPlaces.stream()
                .filter(place -> !hasPreference || place.score > 0)
                .sorted(Comparator.comparingInt(RankedPlace::score).reversed()
                        .thenComparing(RankedPlace::title, Comparator.nullsLast(String::compareTo)))
                .limit(maxResults)
                .map(place -> new RecommendedPlace(
                        place.contentId,
                        place.title,
                        place.contentTypeId,
                        place.address,
                        place.firstImage,
                        place.score
                ))
                .toList();
    }

    private JsonNode extractItemsNode(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("response").path("body").path("items").path("item");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse areaBasedSyncList response.", e);
        }
    }

    private RankedPlace toRankedPlace(JsonNode item, PlacePreferenceRequest preference) {
        String title = text(item, "title");
        String contentTypeId = text(item, "contenttypeid");
        String cat1 = text(item, "cat1");
        String cat2 = text(item, "cat2");
        String cat3 = text(item, "cat3");

        int score = 0;
        if (StringUtils.hasText(preference.getPreferredContentTypeId())
                && preference.getPreferredContentTypeId().equals(contentTypeId)) {
            score += 2;
        }

        List<String> keywords = preference.getPreferredKeywords();
        if (keywords != null) {
            String haystack = String.join(" ", safe(title), safe(cat1), safe(cat2), safe(cat3)).toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (StringUtils.hasText(keyword) && haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                    score += 1;
                }
            }
        }

        return new RankedPlace(
                text(item, "contentid"),
                title,
                contentTypeId,
                text(item, "addr1"),
                text(item, "firstimage"),
                score
        );
    }

    private boolean hasPreference(PlacePreferenceRequest preference) {
        if (preference == null) {
            return false;
        }
        if (StringUtils.hasText(preference.getPreferredContentTypeId())) {
            return true;
        }
        List<String> keywords = preference.getPreferredKeywords();
        if (keywords == null) {
            return false;
        }
        return keywords.stream().anyMatch(StringUtils::hasText);
    }

    private int resolveMaxResults(PlacePreferenceRequest preference) {
        if (preference == null || preference.getMaxResults() == null || preference.getMaxResults() <= 0) {
            return DEFAULT_MAX_RESULTS;
        }
        return preference.getMaxResults();
    }

    private String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record RankedPlace(
            String contentId,
            String title,
            String contentTypeId,
            String address,
            String firstImage,
            int score
    ) {
    }
}



