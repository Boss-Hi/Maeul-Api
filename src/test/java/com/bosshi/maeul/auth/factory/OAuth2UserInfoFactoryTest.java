package com.bosshi.maeul.auth.factory;

import com.bosshi.maeul.auth.dto.*;
import com.bosshi.maeul.auth.type.AuthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2UserInfoFactory 테스트")
class OAuth2UserInfoFactoryTest {

    @InjectMocks
    private OAuth2UserInfoFactory oauth2UserInfoFactory;

    @Test
    @DisplayName("Kakao provider로 KakaoUserInfo를 생성한다")
    void createKakaoUserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 1234567890L);
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "user@kakao.com");
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오사용자");
        kakaoAccount.put("profile", profile);
        attributes.put("kakao_account", kakaoAccount);

        // When
        OAuth2UserInfo userInfo = oauth2UserInfoFactory.createUserInfo("kakao", attributes);

        // Then
        assertThat(userInfo).isInstanceOf(KakaoUserInfo.class);
        assertThat(userInfo.getProviderId()).isEqualTo("1234567890");
        assertThat(userInfo.getEmail()).isEqualTo("user@kakao.com");
        assertThat(userInfo.getNickname()).isEqualTo("카카오사용자");
        assertThat(userInfo.getAuthType()).isEqualTo(AuthType.KAKAO);
    }

    @Test
    @DisplayName("Google provider로 GoogleUserInfo를 생성한다")
    void createGoogleUserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google-user-id-123");
        attributes.put("email", "user@gmail.com");
        attributes.put("name", "Google User");
        attributes.put("given_name", "Google");

        // When
        OAuth2UserInfo userInfo = oauth2UserInfoFactory.createUserInfo("google", attributes);

        // Then
        assertThat(userInfo).isInstanceOf(GoogleUserInfo.class);
        assertThat(userInfo.getProviderId()).isEqualTo("google-user-id-123");
        assertThat(userInfo.getEmail()).isEqualTo("user@gmail.com");
        assertThat(userInfo.getNickname()).isEqualTo("Google User");
        assertThat(userInfo.getAuthType()).isEqualTo(AuthType.GOOGLE);
    }

    @Test
    @DisplayName("Apple provider로 AppleUserInfo를 생성한다")
    void createAppleUserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "apple-user-id-123");
        attributes.put("email", "user@icloud.com");

        // When
        OAuth2UserInfo userInfo = oauth2UserInfoFactory.createUserInfo("apple", attributes);

        // Then
        assertThat(userInfo).isInstanceOf(AppleUserInfo.class);
        assertThat(userInfo.getProviderId()).isEqualTo("apple-user-id-123");
        assertThat(userInfo.getEmail()).isEqualTo("user@icloud.com");
        assertThat(userInfo.getAuthType()).isEqualTo(AuthType.APPLE);
    }

    @Test
    @DisplayName("대소문자를 무시하고 provider를 처리한다")
    void createUserInfoCaseInsensitive() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "test-id");
        attributes.put("email", "test@example.com");
        attributes.put("name", "Test User");

        // When
        OAuth2UserInfo userInfo1 = oauth2UserInfoFactory.createUserInfo("GOOGLE", attributes);
        OAuth2UserInfo userInfo2 = oauth2UserInfoFactory.createUserInfo("Google", attributes);
        OAuth2UserInfo userInfo3 = oauth2UserInfoFactory.createUserInfo("google", attributes);

        // Then
        assertThat(userInfo1).isInstanceOf(GoogleUserInfo.class);
        assertThat(userInfo2).isInstanceOf(GoogleUserInfo.class);
        assertThat(userInfo3).isInstanceOf(GoogleUserInfo.class);
    }

    @Test
    @DisplayName("지원하지 않는 provider는 예외를 던진다")
    void createUserInfoUnsupportedProvider() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "test-id");

        // When & Then
        assertThatThrownBy(() -> oauth2UserInfoFactory.createUserInfo("unsupported", attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 OAuth2 provider");
    }

    @Test
    @DisplayName("null provider는 예외를 던진다")
    void createUserInfoNullProvider() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "test-id");

        // When & Then
        assertThatThrownBy(() -> oauth2UserInfoFactory.createUserInfo(null, attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("provider는 null이거나 비어 있을 수 없습니다");
    }


}


