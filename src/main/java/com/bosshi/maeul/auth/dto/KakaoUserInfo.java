package com.bosshi.maeul.auth.dto;

import java.util.Map;

public class KakaoUserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
    }

    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    public String getEmail() {
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    public String getNickname() {
        return profile != null ? (String) profile.get("nickname") : null;
    }
}
