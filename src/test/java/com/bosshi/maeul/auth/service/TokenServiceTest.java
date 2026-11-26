package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.config.AuthProperties;
import com.bosshi.maeul.auth.entity.RefreshToken;
import com.bosshi.maeul.auth.jwt.JwtTokenProvider;
import com.bosshi.maeul.auth.repository.LoginCodeRepository;
import com.bosshi.maeul.auth.repository.RefreshTokenRepository;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("리프레시 토큰 회전·폐기 테스트")
class TokenServiceTest {

    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final TokenService service = new TokenService(refreshTokenRepository, mock(LoginCodeRepository.class), userRepository,
            jwtTokenProvider, new AuthProperties());
    private final User user = User.builder().id(7L).email("a@b.c").authType("KAKAO").authId("123").build();

    @BeforeEach
    void setUp() {
        when(jwtTokenProvider.generateAccessToken(any(), any())).thenReturn("access");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("발급한 리프레시 토큰은 원문 대신 해시로 저장하고, 액세스 토큰 수명은 1시간이다")
    void issueStoresHash() {
        IssuedTokens tokens = service.issue(user);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo(TokenHasher.sha256(tokens.refreshToken())).isNotEqualTo(tokens.refreshToken());
        assertThat(tokens.accessTokenExpiresIn()).isEqualTo(3600);
        verify(jwtTokenProvider).generateAccessToken(user, Duration.ofHours(1));
    }

    @Test
    @DisplayName("갱신하면 같은 기기(family)로 새 토큰을 발급한다")
    void refreshRotatesWithinFamily() {
        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256("old"))).thenReturn(Optional.of(stored("fam-1", null, null)));
        when(refreshTokenRepository.markUsed(eq(1L), any())).thenReturn(1);

        IssuedTokens tokens = service.refresh("old");

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getFamilyId()).isEqualTo("fam-1");
        assertThat(tokens.refreshToken()).isNotEqualTo("old");
        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
    }

    @Test
    @DisplayName("이미 쓴 리프레시 토큰이 다시 오면 그 기기의 토큰을 모두 폐기하고 거부한다")
    void reuseRevokesFamily() {
        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256("old"))).thenReturn(Optional.of(stored("fam-1", LocalDateTime.now(), null)));
        when(refreshTokenRepository.markUsed(eq(1L), any())).thenReturn(0);

        assertThatThrownBy(() -> service.refresh("old")).isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository).revokeFamily(eq("fam-1"), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("폐기되었거나 만료된 토큰은 거부한다")
    void rejectsRevokedOrExpired() {
        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256("revoked"))).thenReturn(Optional.of(stored("f", null, LocalDateTime.now())));
        RefreshToken expired = stored("f", null, null);
        expired.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256("expired"))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.refresh("revoked")).isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.refresh("expired")).isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.refresh("unknown")).isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository, never()).markUsed(any(), any());
    }

    @Test
    @DisplayName("로그아웃은 그 기기(family)만 폐기한다")
    void logoutRevokesFamily() {
        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256("tok"))).thenReturn(Optional.of(stored("fam-9", null, null)));

        service.logout("tok");

        verify(refreshTokenRepository).revokeFamily(eq("fam-9"), any());
        verify(refreshTokenRepository, never()).revokeUser(any(), any());
    }

    private static RefreshToken stored(String familyId, LocalDateTime usedAt, LocalDateTime revokedAt) {
        return RefreshToken.builder().id(1L).userId(7L).familyId(familyId).tokenHash("h")
                .expiresAt(LocalDateTime.now().plusDays(1)).usedAt(usedAt).revokedAt(revokedAt).build();
    }
}
