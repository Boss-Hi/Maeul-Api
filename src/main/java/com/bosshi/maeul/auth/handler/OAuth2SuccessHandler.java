package com.bosshi.maeul.auth.handler;

import com.bosshi.maeul.auth.dto.OAuth2UserInfo;
import com.bosshi.maeul.auth.factory.OAuth2UserInfoFactory;
import com.bosshi.maeul.auth.service.AccountService;
import com.bosshi.maeul.auth.config.AuthProperties;
import com.bosshi.maeul.auth.jwt.JwtTokenProvider;
import com.bosshi.maeul.auth.service.TokenService;
import com.bosshi.maeul.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * 웹 OAuth 로그인(/oauth2/authorization/kakao) 성공 (ADR 0015).
 * 네이티브 앱이 부른 경우(User-Agent 에 auth.native-app-user-agent 가 들어 있음)는 짧게 사는 1회용 코드를 URL 에 실어 돌려보내고,
 * 앱은 POST /api/auth/code/exchange 로 액세스 토큰과 리프레시 토큰으로 바꾼다.
 * 그 밖의 경우(웹 브라우저)는 기존 방식대로 액세스 토큰을 token 쿼리로 바로 준다.
 */
@Component
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final AccountService accountService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthProperties properties;
    private final OAuth2UserInfoFactory userInfoFactory;
    private final String redirectBaseUrl;

    public OAuth2SuccessHandler(
            AccountService accountService,
            TokenService tokenService,
            JwtTokenProvider jwtTokenProvider,
            AuthProperties properties,
            OAuth2UserInfoFactory userInfoFactory,
            @Value("${server.oauth2.redirect-url:http://localhost:8080/oauth/redirect}") String redirectBaseUrl
    ) {
        this.accountService = accountService;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.properties = properties;
        this.userInfoFactory = userInfoFactory;
        this.redirectBaseUrl = redirectBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2UserInfo userInfo = userInfoFactory.createUserInfo(token.getAuthorizedClientRegistrationId(), token.getPrincipal().getAttributes());
        User user = accountService.findOrCreate(userInfo.getAuthType(), userInfo.getProviderId(), userInfo.getNickname());
        log.info("OAuth2 로그인 성공: {}({}) userId={}", userInfo.getAuthType(), userInfo.getProviderId(), user.getId());

        UriComponentsBuilder redirect = UriComponentsBuilder.fromUriString(redirectBaseUrl);
        if (isNativeApp(request)) {
            redirect.queryParam("code", tokenService.createLoginCode(user.getId()));
        } else {
            redirect.queryParam("token", jwtTokenProvider.generateAccessToken(user, properties.getToken().getWebAccessTtl()));
        }
        response.sendRedirect(redirect.build().toUriString());
    }

    /** 네이티브 앱의 WebView 가 User-Agent 에 붙이는 표식. 리다이렉트를 거쳐도 유지되므로 별도 상태를 저장하지 않는다 */
    boolean isNativeApp(HttpServletRequest request) {
        String marker = properties.getNativeAppUserAgent();
        String userAgent = request.getHeader("User-Agent");
        return marker != null && !marker.isBlank() && userAgent != null
                && userAgent.toLowerCase(java.util.Locale.ROOT).contains(marker.toLowerCase(java.util.Locale.ROOT));
    }
}
