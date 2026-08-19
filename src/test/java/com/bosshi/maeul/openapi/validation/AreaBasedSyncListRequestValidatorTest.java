package com.bosshi.maeul.openapi.validation;

import com.bosshi.maeul.openapi.request.AreaBasedSyncListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

import static org.assertj.core.api.Assertions.assertThat;

class AreaBasedSyncListRequestValidatorTest {

    private final AreaBasedSyncListRequestValidator validator = new AreaBasedSyncListRequestValidator();

    @Test
    void validatesDependentParams() {
        AreaBasedSyncListRequest request = new AreaBasedSyncListRequest();
        request.setLDongSignguCd("380");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "areaBasedSyncListRequest");
        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getAllErrors().get(0).getDefaultMessage())
                .isEqualTo("lDongRegnCd is required when lDongSignguCd is provided.");
    }

    @Test
    void passesWhenDependenciesSatisfied() {
        AreaBasedSyncListRequest request = new AreaBasedSyncListRequest();
        request.setLDongRegnCd("26");
        request.setLDongSignguCd("380");
        request.setLclsSystm1("NA");
        request.setLclsSystm2("NA04");
        request.setLclsSystm3("NA040500");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "areaBasedSyncListRequest");
        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}

