package com.bosshi.maeul.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {
    // Spring Boot가 build-info.properties를 읽어 자동으로 주입해줍니다.
    private final BuildProperties buildProperties;

    @GetMapping("/")
    public String index() {
        // build.gradle에 명시된 프로젝트 버전을 가져옵니다.
        String version = buildProperties.getVersion();

        return "Welcome to the Maeul API " + version;
    }
}
