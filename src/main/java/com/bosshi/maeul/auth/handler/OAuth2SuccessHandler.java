package com.bosshi.maeul.auth.handler;

import com.bosshi.maeul.auth.dto.OAuth2UserInfo;
import com.bosshi.maeul.auth.factory.OAuth2UserInfoFactory;
import com.bosshi.maeul.auth.service.AccountService;
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
 * 웹 OAuth 로그인 성공: 토큰 대신 짧게 사는 1회용 코드를 URL 에 실어 웹앱으로 돌려보낸다 (ADR 0015).
 * 웹앱은 POST /api/auth/code/exchange 로 코드를 액세스 토큰과 리프레시 토큰으로 바꾼다.
 */
@Component
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final AccountService accountService;
    private final TokenService tokenService;
    private final OAuth2UserInfoFactory userInfoFactory;
    private final String redirectBaseUrl;

    public OAuth2SuccessHandler(
            AccountService accountService,
            TokenService tokenService,
            OAuth2UserInfoFactory userInfoFactory,
            @Value("${server.oauth2.redirect-url:http://localhost:8080/oauth/redirect}") String redirectBaseUrl
    ) {
        this.accountService = accountService;
        this.tokenService = tokenService;
        this.userInfoFactory = userInfoFactory;
        this.redirectBaseUrl = redirectBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2UserInfo userInfo = userInfoFactory.createUserInfo(token.getAuthorizedClientRegistrationId(), token.getPrincipal().getAttributes());
        User user = accountService.findOrCreate(userInfo.getAuthType(), userInfo.getProviderId(), userInfo.getNickname());
        log.info("OAuth2 로그인 성공: {}({}) userId={}", userInfo.getAuthType(), userInfo.getProviderId(), user.getId());

        response.sendRedirect(UriComponentsBuilder.fromUriString(redirectBaseUrl)
                .queryParam("code", tokenService.createLoginCode(user.getId()))
                .build()
                .toUriString());
    }
}
