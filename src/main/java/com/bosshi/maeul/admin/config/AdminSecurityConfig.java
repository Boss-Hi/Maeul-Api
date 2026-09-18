package com.bosshi.maeul.admin.config;

import com.bosshi.maeul.admin.filter.AdminPathDomainCheckFilter;
import com.bosshi.maeul.admin.service.AdminUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Admin (KraftAdmin) 전용 Security 설정.
 * - 우선순위: @Order(1)
 * - /admin/** 및 /admin/api/** 경로 대상
 */
@Configuration("maeulAdminSecurityConfig")
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final AdminPathDomainCheckFilter adminPathDomainCheckFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**", "/admin/api/**", "/admin")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .addFilterBefore(adminPathDomainCheckFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider(
            AdminUserDetailsService adminUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
