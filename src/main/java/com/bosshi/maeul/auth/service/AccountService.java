package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.type.AuthType;
import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * 카카오·Apple 계정 찾기 (ADR 0015). 계정은 (로그인 수단, 그 수단의 사용자 ID)로 찾고, 없으면 만든다.
 * 이메일은 계정 식별에 쓰지 않으며, 실제 이메일이 없으므로 {수단}_{ID}@{수단}.local 을 넣는다.
 */
@Slf4j
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** PasswordEncoder 는 SecurityConfig 가 만들고, SecurityConfig 는 이 서비스를 쓰는 OAuth2SuccessHandler 를 쓴다. 순환을 끊기 위해 지연 주입한다 */
    public AccountService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> find(AuthType authType, String providerUserId) {
        return userRepository.findByAuthTypeAndAuthId(authType.name(), providerUserId);
    }

    @Transactional
    public User findOrCreate(AuthType authType, String providerUserId, String nickname) {
        return find(authType, providerUserId).orElseGet(() -> create(authType, providerUserId, nickname));
    }

    private User create(AuthType authType, String providerUserId, String nickname) {
        String provider = authType.name().toLowerCase(Locale.ROOT);
        User user = new User();
        user.setEmail(provider + "_" + providerUserId + "@" + provider + ".local");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setAuthType(authType.name());
        user.setAuthId(providerUserId);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setNickname(nickname != null && !nickname.isBlank() ? nickname : "User_" + authType.name());
        user.setProfile(profile);

        User saved = userRepository.save(user);
        log.info("신규 {}({}) 사용자 등록: userId={}", authType, providerUserId, saved.getId());
        return saved;
    }
}
