package com.bosshi.maeul.auth.config;

import com.bosshi.maeul.auth.filter.JwtAuthenticationFilter;
import com.bosshi.maeul.auth.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

/**
 * 일반 사용자 및 공용 API 전용 Security 설정.
 * - 우선순위: @Order(2)
 * - JWT Stateless 인증 방식 및 OAuth2 소셜 로그인 연동
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth & OAuth2
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/oauth2/**").permitAll()
                        // 모니터링
                        .requestMatchers("/actuator/**").permitAll()
                        // location
                        .requestMatchers("/api/location").permitAll()
                        // 관광
                        .requestMatchers("/api/tours/**", "/api/tour-category-types/**", "/api/tour-categories/**").permitAll()
                        // 축제
                        .requestMatchers("/api/festivals/**").permitAll()
                        // 일정
                        .requestMatchers("/api/open/**", "/api/itinerary/**").permitAll()
                        // 설문
                        .requestMatchers(HttpMethod.GET, "/api/surveys/first").permitAll()
                        // 관리자 전용 API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 루트 페이지, 에러
                        .requestMatchers("/", "/error", "/index.html").permitAll()
                        .anyRequest().authenticated()
                );

        if (clientRegistrationRepository.isPresent()) {
            http.oauth2Login(oauth2 -> oauth2
                    .clientRegistrationRepository(clientRegistrationRepository.get())
                    .defaultSuccessUrl("/", true)
                    .successHandler(oAuth2SuccessHandler)
            );
        }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager Bean 노출
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
