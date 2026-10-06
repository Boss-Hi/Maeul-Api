package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.dto.OAuth2UserInfo;
import com.bosshi.maeul.auth.factory.OAuth2UserInfoFactory;
import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * OAuth2 사용자 로그인을 처리하는 서비스.
 * Strategy 패턴을 사용하여 다양한 OAuth2 provider(Kakao, Google, Apple 등)를 지원한다.
 * Factory 패턴을 사용하여 provider별 사용자 정보 처리 로직을 분리한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
     * OAuth2 사용자 정보를 기반으로 사용자를 저장하거나 업데이트한다.
     * 이메일이 없으면 providerId를 기반으로 임시 이메일을 생성한다.
     *
     * @param oauth2UserInfo provider에서 추출한 사용자 정보
     */
    private void saveOrUpdateUser(OAuth2UserInfo oauth2UserInfo) {
        String email = oauth2UserInfo.getEmail();

        // 이메일이 없으면 providerId 기반으로 임시 이메일 생성
        if (email == null || email.isBlank()) {
            email = oauth2UserInfo.getAuthType().name().toLowerCase() + "_" +
                    oauth2UserInfo.getProviderId() + "@" +
                    oauth2UserInfo.getAuthType().name().toLowerCase() + ".local";
        }

        // DB에 사용자 존재 여부 확인 후 없으면 신규 생성
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            // OAuth2 사용자는 랜덤 비밀번호(암호화)로 저장
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setAuthType(oauth2UserInfo.getAuthType().name());
            user.setAuthId(oauth2UserInfo.getProviderId());

            Profile profile = new Profile();
            profile.setUser(user);
            profile.setNickname(oauth2UserInfo.getNickname() != null ?
                    oauth2UserInfo.getNickname() : "User_" + oauth2UserInfo.getAuthType().name());

            user.setProfile(profile);
            userRepository.save(user);

            log.info("신규 {}({}) 사용자 등록: {}",
                    oauth2UserInfo.getAuthType().name(), oauth2UserInfo.getProviderId(), email);
        } else {
            log.info("기존 {}({}) 사용자 로그인: {}",
                    oauth2UserInfo.getAuthType().name(), oauth2UserInfo.getProviderId(), email);
        }
    }
}

