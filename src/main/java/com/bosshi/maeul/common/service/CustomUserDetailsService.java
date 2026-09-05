package com.bosshi.maeul.common.service;

import com.bosshi.maeul.common.security.CustomUserDetails;
import com.bosshi.maeul.user.domain.User;
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
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        Collection<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), auth);
    }
}
