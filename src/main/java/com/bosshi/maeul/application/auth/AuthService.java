package com.bosshi.maeul.application.auth;

import com.bosshi.maeul.api.auth.LoginRequest;
import com.bosshi.maeul.api.auth.RegisterRequest;
import com.bosshi.maeul.common.jwt.JwtTokenProvider;
import com.bosshi.maeul.user.domain.Profile;
import com.bosshi.maeul.user.domain.User;
import com.bosshi.maeul.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setNickname(request.nickname());

        user.setProfile(profile);
        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtTokenProvider.generateToken(user.getEmail());
    }
}
