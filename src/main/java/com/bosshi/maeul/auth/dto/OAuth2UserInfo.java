package com.bosshi.maeul.auth.dto;

import com.bosshi.maeul.auth.type.AuthType;

/**
 * OAuth2 사용자 정보를 추상화한 인터페이스.
 * 각 OAuth2 provider별 구현체가 이 인터페이스를 구현한다.
 */
public interface OAuth2UserInfo {

    /**
     * Provider에서 제공하는 고유 사용자 ID를 반환한다.
     */
    String getProviderId();

    /**
     * 사용자 이메일을 반환한다.
     */
    String getEmail();

    /**
     * 사용자 닉네임/이름을 반환한다.
     */
    String getNickname();

    /**
     * 이 정보를 제공한 OAuth2 provider 타입을 반환한다.
     */
    AuthType getAuthType();

    /**
     * 해당 provider의 원본 attributes를 반환한다.
     */
    Object getAttributes();
}

