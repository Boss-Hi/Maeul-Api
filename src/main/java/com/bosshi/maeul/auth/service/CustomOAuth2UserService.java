package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.dto.KakaoUserInfo;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // kakao
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        if ("kakao".equals(registrationId)) {
            KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
            log.info(
                    "카카오 로그인 성공 - ID: {}, Email: {}, Nickname: {}",
                    kakaoUserInfo.getProviderId(), kakaoUserInfo.getEmail(), kakaoUserInfo.getNickname()
            );

            // 이메일이 없으면 providerId 기반으로 임시 이메일 생성
            String email = kakaoUserInfo.getEmail();
            if (email == null || email.isBlank()) {
                email = "kakao_" + kakaoUserInfo.getProviderId() + "@kakao.local";
            }

            // DB에 사용자 존재 여부 확인 후 없으면 신규 생성
            if (!userRepository.existsByEmail(email)) {
                User user = new User();
                user.setEmail(email);
                // OAuth2 사용자는 랜덤 비밀번호(암호화)로 저장
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                Profile profile = new Profile();
                profile.setUser(user);
                profile.setNickname(kakaoUserInfo.getNickname() != null ? kakaoUserInfo.getNickname() : "KakaoUser");

                user.setProfile(profile);
                userRepository.save(user);

                log.info("신규 사용자 등록: {}", email);
            } else {
                log.info("기존 사용자 로그인: {}", email);
            }
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }
}