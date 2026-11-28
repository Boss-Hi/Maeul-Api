package com.bosshi.maeul.auth.handler;

import com.bosshi.maeul.auth.config.AuthProperties;
import com.bosshi.maeul.auth.dto.OAuth2UserInfo;
import com.bosshi.maeul.auth.factory.OAuth2UserInfoFactory;
import com.bosshi.maeul.auth.jwt.JwtTokenProvider;
import com.bosshi.maeul.auth.service.AccountService;
import com.bosshi.maeul.auth.service.TokenService;
import com.bosshi.maeul.auth.type.AuthType;
import com.bosshi.maeul.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.AuthorityUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OAuth2SuccessHandler 웹 OAuth 로그인 성공")
class OAuth2SuccessHandlerTest {

    private final AccountService accountService = mock(AccountService.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final OAuth2UserInfoFactory factory = mock(OAuth2UserInfoFactory.class);
    private final AuthProperties properties = new AuthProperties();
    private OAuth2SuccessHandler handler;
    private OAuth2AuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        handler = new OAuth2SuccessHandler(accountService, tokenService, jwtTokenProvider, properties, factory, "https://web.example.com/oauth/redirect");
        OAuth2UserInfo info = mock(OAuth2UserInfo.class);
        when(info.getAuthType()).thenReturn(AuthType.KAKAO);
        when(info.getProviderId()).thenReturn("123");
        when(info.getNickname()).thenReturn("닉");
        when(factory.createUserInfo(eq("kakao"), any())).thenReturn(info);
        User user = new User();
        user.setId(9L);
        when(accountService.findOrCreate(AuthType.KAKAO, "123", "닉")).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(any(), any())).thenReturn("ACCESS.JWT");
        when(tokenService.createLoginCode(9L)).thenReturn("ONE-TIME");
        authentication = new OAuth2AuthenticationToken(
                new DefaultOAuth2User(AuthorityUtils.NO_AUTHORITIES, Map.of("id", 123), "id"), AuthorityUtils.NO_AUTHORITIES, "kakao");
    }

    private String redirectFor(String userAgent) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, authentication);
        return response.getRedirectedUrl();
    }

    @Test
    @DisplayName("네이티브 앱이 아니면 기존 방식대로 액세스 토큰을 token 쿼리로 바로 준다")
    void browserGetsAccessTokenDirectly() throws Exception {
        assertThat(redirectFor("Mozilla/5.0 (iPhone) Safari/604")).isEqualTo("https://web.example.com/oauth/redirect?token=ACCESS.JWT");
        assertThat(redirectFor(null)).isEqualTo("https://web.example.com/oauth/redirect?token=ACCESS.JWT");
        verify(jwtTokenProvider, atLeastOnce()).generateAccessToken(any(User.class), eq(Duration.ofHours(10)));
        verify(tokenService, never()).createLoginCode(any());
    }

    @Test
    @DisplayName("네이티브 앱의 User-Agent 표식이 있으면 1회용 코드를 준다")
    void nativeAppGetsOneTimeCode() throws Exception {
        assertThat(redirectFor("Mozilla/5.0 (Linux; Android 14) MaeulApp/1.2.0")).isEqualTo("https://web.example.com/oauth/redirect?code=ONE-TIME");
        assertThat(redirectFor("mozilla/5.0 maeulapp")).isEqualTo("https://web.example.com/oauth/redirect?code=ONE-TIME");
        verify(jwtTokenProvider, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("표식 설정이 비어 있으면 모두 기존 방식이다")
    void blankMarkerMeansNeverNative() throws Exception {
        properties.setNativeAppUserAgent("");

        assertThat(redirectFor("MaeulApp/1.0")).isEqualTo("https://web.example.com/oauth/redirect?token=ACCESS.JWT");
    }
}
