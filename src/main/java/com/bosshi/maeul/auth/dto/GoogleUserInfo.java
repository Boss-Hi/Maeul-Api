package com.bosshi.maeul.auth.dto;

import com.bosshi.maeul.auth.type.AuthType;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보를 처리하는 클래스.
 * OAuth2UserInfo 인터페이스를 구현하여 일관된 방식으로 사용자 정보를 제공한다.
 */
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
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
        String name = (String) attributes.get("name");
        return name != null ? name : (String) attributes.get("given_name");
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.GOOGLE;
    }

    @Override
    public Object getAttributes() {
        return attributes;
    }
}

