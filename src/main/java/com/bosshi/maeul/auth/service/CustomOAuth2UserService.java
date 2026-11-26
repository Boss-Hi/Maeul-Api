package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.dto.OAuth2UserInfo;
import com.bosshi.maeul.auth.factory.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * OAuth2 사용자 로그인을 처리하는 서비스.
 * Strategy 패턴을 사용하여 다양한 OAuth2 provider(Kakao, Google, Apple 등)를 지원한다.
 * Factory 패턴을 사용하여 provider별 사용자 정보 처리 로직을 분리한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AccountService accountService;
    private final OAuth2UserInfoFactory oauth2UserInfoFactory;

    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        try {
            // Factory를 사용하여 provider별 OAuth2UserInfo 생성
            OAuth2UserInfo oauth2UserInfo = oauth2UserInfoFactory.createUserInfo(
                    registrationId,
                    oAuth2User.getAttributes()
            );

            log.info(
                    "OAuth2 로그인 성공 - Provider: {}, ID: {}, Email: {}, Nickname: {}",
                    registrationId.toUpperCase(), oauth2UserInfo.getProviderId(),
                    oauth2UserInfo.getEmail(), oauth2UserInfo.getNickname()
            );

            // 사용자 등록 또는 조회
            saveOrUpdateUser(oauth2UserInfo);

        } catch (IllegalArgumentException e) {
            log.error("지원하지 않는 OAuth2 provider: {}", registrationId);
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 provider입니다: " + registrationId);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }

    /**
     * (로그인 수단, 그 수단의 사용자 ID)로 계정을 찾고, 없으면 만든다 (ADR 0015).
     */
    private void saveOrUpdateUser(OAuth2UserInfo oauth2UserInfo) {
        accountService.findOrCreate(oauth2UserInfo.getAuthType(), oauth2UserInfo.getProviderId(), oauth2UserInfo.getNickname());
    }
}
