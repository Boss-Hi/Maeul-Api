package com.bosshi.maeul.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 공공데이터포털 Tour API 요청 파라미터 공통 부모 클래스.
 */
@Getter
@Setter
public abstract class OpenApiBaseRequest {
    /** 한 페이지 결과 수 (기본: 10) */
    private Integer numOfRows = 10;
    /** 페이지 번호 (기본: 1) */
    private Integer pageNo = 1;

    /**
     * 클래스 및 상위 클래스의 프로퍼티(fields)를 읽어 MultiValueMap으로 변환한다.
     * null 또는 빈 문자열인 값은 제외한다.
     */
    public MultiValueMap<String, String> toQueryParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = getClass();
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);

        for (Class<?> clazz : hierarchy) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    if (value != null) {
                        String strVal = String.valueOf(value).trim();
                        if (StringUtils.hasText(strVal)) {
                            params.add(field.getName(), strVal);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to read property: " + field.getName(), e);
                }
            }
        }

        return params;
    }
}
