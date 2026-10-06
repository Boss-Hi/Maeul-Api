package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.auth.type.AuthType;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Security의 UserDetailsService 구현체.
 * 이메일로 사용자를 조회하고 UserDetails를 반환한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * 이메일로 사용자를 조회한다. 없으면 UsernameNotFoundException을 던진다.
     */
    @Override
    public UserDetails loadUserByUsername(@NonNull String emailOrAuthId) throws UsernameNotFoundException {
        ResolvedPrincipal resolvedPrincipal = resolvePrincipal(emailOrAuthId);
        Optional<User> userOptional = AuthType.BASIC.name().equals(resolvedPrincipal.authType())
                ? Optional.ofNullable(userRepository.findByEmail(resolvedPrincipal.identifier()))
                : userRepository.findByAuthTypeAndAuthId(resolvedPrincipal.authType(), resolvedPrincipal.identifier());

        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        String userAuthType = user.getAuthType() != null ? user.getAuthType() : AuthType.BASIC.name();
        if (!resolvedPrincipal.authType().equals(userAuthType)) {
            throw new UsernameNotFoundException("인증 타입이 일치하지 않습니다.");
        }

        Collection<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), userAuthType, auth);
    }

    private ResolvedPrincipal resolvePrincipal(String emailOrAuthId) {
        if (emailOrAuthId == null || emailOrAuthId.isBlank()) {
            throw new UsernameNotFoundException("인증 식별자가 비어 있습니다.");
        }

        String principal = emailOrAuthId.trim();
        int delimiterIndex = principal.indexOf(':');
        if (delimiterIndex > 0 && delimiterIndex < principal.length() - 1) {
            String provider = principal.substring(0, delimiterIndex).trim().toUpperCase();
            String identifier = principal.substring(delimiterIndex + 1).trim();
            AuthType resolvedType = parseAuthType(provider);
            return new ResolvedPrincipal(resolvedType.name(), identifier);
        }

        return new ResolvedPrincipal(resolveAuthType(principal), principal);
    }

    private String resolveAuthType(String emailOrAuthId) {
        if (emailOrAuthId.contains("@")) {
            return AuthType.BASIC.name();
        }

        throw new UsernameNotFoundException("소셜 로그인 식별자는 'PROVIDER:identifier' 형식이어야 합니다. 예: GOOGLE:abc123");
    }

    private AuthType parseAuthType(String provider) {
        try {
            return AuthType.valueOf(provider);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("지원하지 않는 인증 타입입니다: " + provider);
        }
    }

    private record ResolvedPrincipal(String authType, String identifier) { }
}
