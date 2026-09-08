package com.bosshi.maeul.auth.handler;

import com.bosshi.maeul.auth.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtProvider;
    private final String redirectBaseUrl;

    public OAuth2SuccessHandler(
            JwtTokenProvider jwtProvider,
            @Value("${server.oauth2.redirect-url}") String redirectBaseUrl
    ) {
        this.jwtProvider = jwtProvider;
        this.redirectBaseUrl = redirectBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2 로그인 성공: {}", oAuth2User.getAttributes());

        String accessToken = jwtProvider.generateToken(oAuth2User);

        // env/yml에서 주입받은 URL을 기반으로 쿼리 파라미터 조합
        String redirectUrl = UriComponentsBuilder.fromUriString(redirectBaseUrl)
                .queryParam("token", accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}