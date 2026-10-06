package com.bosshi.maeul.auth.dto;

import com.bosshi.maeul.auth.type.AuthType;

import java.util.Map;

/**
 * Apple OAuth2 사용자 정보를 처리하는 클래스.
 * OAuth2UserInfo 인터페이스를 구현하여 일관된 방식으로 사용자 정보를 제공한다.
 */
public class AppleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @SuppressWarnings("unchecked")
    public AppleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        // Apple은 사용자 이름 정보를 제공하지 않으므로, email 기반으로 생성
        String email = getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "AppleUser";
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.APPLE;
    }

    @Override
    public Object getAttributes() {
        return attributes;
    }
}

