package com.bosshi.maeul.auth.dto;

import com.bosshi.maeul.auth.type.AuthType;

import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보를 처리하는 클래스.
 * OAuth2UserInfo 인터페이스를 구현하여 일관된 방식으로 사용자 정보를 제공한다.
 */
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override
    public String getNickname() {
        return profile != null ? (String) profile.get("nickname") : null;
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.KAKAO;
    }

    @Override
    public Object getAttributes() {
        return attributes;
    }
}
