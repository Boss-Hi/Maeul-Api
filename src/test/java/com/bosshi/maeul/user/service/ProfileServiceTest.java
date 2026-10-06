package com.bosshi.maeul.user.service;

import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.ProfileRepository;
import com.bosshi.maeul.user.repository.UserRepository;
import com.bosshi.maeul.user.request.ProfileUpdateRequest;
import com.bosshi.maeul.user.response.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getMyProfileReturnsStoredProfile() {
        User user = createUser(1L, "tester@example.com");
        Profile profile = createProfile(user, "테스터", "소개", "https://image.com/avatar.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        ProfileResponse response = profileService.getMyProfile(1L);

        assertEquals(1L, response.userId());
        assertEquals("tester@example.com", response.email());
        assertEquals("테스터", response.nickname());
        assertEquals("소개", response.bio());
        assertEquals("https://image.com/avatar.png", response.avatarUrl());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void updateMyProfileUpdatesOnlyProvidedFields() {
        User user = createUser(1L, "tester@example.com");
        Profile profile = createProfile(user, "기존닉", "기존소개", "https://image.com/origin.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);

        ProfileUpdateRequest request = new ProfileUpdateRequest(" 새닉네임 ", null, "https://image.com/new.png");

        ProfileResponse response = profileService.updateMyProfile(1L, request);

        assertEquals("새닉네임", response.nickname());
        assertEquals("기존소개", response.bio());
        assertEquals("https://image.com/new.png", response.avatarUrl());
    }

    @Test
    void updateMyProfileThrowsWhenNicknameIsBlank() {
        User user = createUser(1L, "tester@example.com");
        Profile profile = createProfile(user, "기존닉", "소개", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        ProfileUpdateRequest request = new ProfileUpdateRequest("   ", null, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.updateMyProfile(1L, request)
        );

        assertEquals("닉네임은 비어 있을 수 없습니다.", exception.getMessage());
    }

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private Profile createProfile(User user, String nickname, String bio, String avatarUrl) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setNickname(nickname);
        profile.setBio(bio);
        profile.setAvatarUrl(avatarUrl);
        return profile;
    }
}

