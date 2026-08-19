package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 공공데이터포털 Tour API 공통 응답 DTO 부모 클래스.
 *
 * @param <T> 단일 아이템 타입
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class OpenApiBaseResponse<T> {
    /** API 최상위 응답 래퍼 */
    private Response<T> response;

    /** 응답의 헤더와 바디를 담는 내부 응답 객체 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response<T> {
        /** 요청 결과 코드와 메시지 */
        private Header header;
        /** 결과 본문 */
        private Body<T> body;
    }

    /** 결과 코드와 결과 메시지 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    /** 검색 결과 본문 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body<T> {
        /** 결과 목록 */
        @JsonDeserialize(using = ItemsDeserializer.class)
        private Items<T> items = new Items<>();
        /** 요청한 페이지당 결과 수 */
        private Integer numOfRows;
        /** 요청 페이지 번호 */
        private Integer pageNo;
        /** 전체 결과 수 */
        private Integer totalCount;
    }

    /** 결과 목록 배열 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items<T> {
        private List<T> item = new ArrayList<>();
    }

    /**
     * 공공데이터포털(Tour API) 응답의 items 필드를 처리하는 커스텀 역직렬화기.
     *
     * <p>주요 처리 케이스:</p>
     * <ul>
     *   <li>데이터가 없을 때: {@code "items": ""} (빈 문자열) 형태로 내려오는 경우 빈 {@link Items} 반환</li>
     *   <li>단일 데이터일 때: {@code "item": { ... }} (단일 JSON 객체)를 {@code List<T>}로 감싸서 반환</li>
     *   <li>복수 데이터일 때: {@code "item": [ { ... }, { ... } ]} (JSON 배열)을 {@code List<T>}로 매핑</li>
     * </ul>
     */
    public static class ItemsDeserializer extends JsonDeserializer<Items<?>> implements ContextualDeserializer {
        /** 제네릭 타입 T (예: SearchFestivalResponse.Item, AreaTarSvcDemListResponse.Item) */
        private JavaType itemType;

        /** Jackson 기본 생성자 */
        public ItemsDeserializer() {}

        /**
         * 제네릭 아이템 타입을 주입받는 생성자
         *
         * @param itemType 역직렬화 대상 Item의 JavaType
         */
        public ItemsDeserializer(JavaType itemType) {
            this.itemType = itemType;
        }

        /**
         * Jackson이 Body&lt;T&gt;의 필드 타입을 분석할 때 호출되는 메서드.
         * <p>
         * 상위 클래스의 제네릭 바인딩 정보(T)를 런타임에 추출하여,
         * 실제 역직렬화 시 대상 Item 클래스 타입으로 매핑할 수 있도록 {@link ItemsDeserializer} 인스턴스를 생성한다.
         * </p>
         */
        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
            JavaType innerType = (wrapperType != null && wrapperType.hasGenericTypes())
                    ? wrapperType.getBindings().getBoundType(0)
                    : ctxt.constructType(Object.class);
            return new ItemsDeserializer(innerType);
        }

        /**
         * JSON 노드를 분석하여 {@link Items} 객체로 변환한다.
         */
        @Override
        public Items<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            // 1. items 필드가 null이거나 빈 문자열("") 등 객체가 아닌 경우 빈 Items 반환
            if (node == null || !node.isObject()) {
                return new Items<>();
            }

            JsonNode itemNode = node.get("item");
            // 2. item 필드가 없거나 null인 경우 빈 Items 반환
            if (itemNode == null || itemNode.isNull()) {
                return new Items<>();
            }

            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            Items<Object> items = new Items<>();

            // 3. item이 배열(Array)인 경우: List<T> 형태로 변환
            if (itemNode.isArray()) {
                JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, itemType);
                items.setItem(mapper.convertValue(itemNode, listType));
            }
            // 4. item이 단일 객체(Object)인 경우: 단일 객체를 List<T>로 감싸서 일관된 리스트로 반환
            else if (itemNode.isObject()) {
                Object single = mapper.convertValue(itemNode, itemType);
                List<Object> list = new ArrayList<>();
                list.add(single);
                items.setItem(list);
            }

            return items;
        }
    }
}
