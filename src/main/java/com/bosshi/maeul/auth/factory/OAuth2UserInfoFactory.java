package com.bosshi.maeul.auth.factory;

import com.bosshi.maeul.auth.dto.*;
import com.bosshi.maeul.auth.type.AuthType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OAuth2UserInfo Factory.
 * OAuth2 provider에 따라 적절한 OAuth2UserInfo 구현체를 생성하고 반환한다.
 * 새로운 provider를 추가할 때는 createUserInfo 메서드에 케이스를 추가하면 된다.
 */
@Component
@Slf4j
public class OAuth2UserInfoFactory {

    /**
     * provider 문자열과 attributes를 받아서 해당하는 OAuth2UserInfo 구현체를 반환한다.
     *
     * @param provider     OAuth2 provider 이름 (kakao, google, apple 등)
     * @param attributes   provider에서 제공한 사용자 정보 attributes
     * @return OAuth2UserInfo 구현체
     * @throws IllegalArgumentException 지원하지 않는 provider인 경우
     */
    public OAuth2UserInfo createUserInfo(String provider, Map<String, Object> attributes) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("OAuth2 provider는 null이거나 비어 있을 수 없습니다.");
        }

        String upperProvider = provider.toUpperCase().trim();
        log.info("OAuth2UserInfo 생성 중 - Provider: {}", upperProvider);

        return switch (upperProvider) {
            case "KAKAO" -> {
                log.debug("Kakao OAuth2 사용자 정보 처리");
                yield new KakaoUserInfo(attributes);
            }
            case "GOOGLE" -> {
                log.debug("Google OAuth2 사용자 정보 처리");
                yield new GoogleUserInfo(attributes);
            }
            case "APPLE" -> {
                log.debug("Apple OAuth2 사용자 정보 처리");
                yield new AppleUserInfo(attributes);
            }
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth2 provider: " + provider);
        };
    }
}


