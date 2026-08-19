package com.bosshi.maeul.openapi.validation;

import com.bosshi.maeul.openapi.request.AreaBasedSyncListRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * areaBasedSyncList2 요청 파라미터의 상호 의존 규칙을 검증한다.
 */
@Component
public class AreaBasedSyncListRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return AreaBasedSyncListRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AreaBasedSyncListRequest request = (AreaBasedSyncListRequest) target;

        if (StringUtils.hasText(request.getLDongSignguCd()) && !StringUtils.hasText(request.getLDongRegnCd())) {
            errors.rejectValue("lDongRegnCd", "required", "lDongRegnCd is required when lDongSignguCd is provided.");
        }
        if (StringUtils.hasText(request.getLclsSystm2()) && !StringUtils.hasText(request.getLclsSystm1())) {
            errors.rejectValue("lclsSystm1", "required", "lclsSystm1 is required when lclsSystm2 is provided.");
        }
        if (StringUtils.hasText(request.getLclsSystm3())
                && (!StringUtils.hasText(request.getLclsSystm1()) || !StringUtils.hasText(request.getLclsSystm2()))) {
            errors.rejectValue("lclsSystm3", "required", "lclsSystm1 and lclsSystm2 are required when lclsSystm3 is provided.");
        }
    }
}

