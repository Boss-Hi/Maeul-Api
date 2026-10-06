package com.bosshi.maeul.user.service;

import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.ProfileRepository;
import com.bosshi.maeul.user.repository.UserRepository;
import com.bosshi.maeul.user.request.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
    private static final int NICKNAME_MAX_LENGTH = 30;
    private static final int DESCRIPTION_MAX_LENGTH = 1024;
    private static final int AVATAR_URL_MAX_LENGTH = 256;

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public Profile getMyProfile(Long userId) {
        return getUserOrThrow(userId).getProfile();
    }

    @Transactional
    public Profile updateMyProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserOrThrow(userId);
        Profile profile = getOrCreateProfile(user, request.nickname());

        if (request.nickname() != null) {
            profile.setNickname(validateNickname(request.nickname()));
        }
        if (request.name() != null) {
            profile.setName(validateLength(request.name(), NICKNAME_MAX_LENGTH, "이름"));
        }
        if (request.description() != null) {
            profile.setDescription(validateLength(request.description(), DESCRIPTION_MAX_LENGTH, "설명"));
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(validateLength(request.avatarUrl(), AVATAR_URL_MAX_LENGTH, "아바타 URL"));
        }
        if (request.year() != null) {
            profile.setYear(request.year());
        }
        if (request.month() != null) {
            profile.setMonth(request.month());
        }
        if (request.day() != null) {
            profile.setDay(request.day());
        }

        return profileRepository.save(profile);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Profile getProfileOrThrow(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    }

    private Profile getOrCreateProfile(User user, String nicknameCandidate) {
        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Profile profile = new Profile();
                    profile.setUser(user);
                    profile.setNickname(resolveInitialNickname(user.getEmail(), nicknameCandidate));
                    user.setProfile(profile);
                    return profileRepository.save(profile);
                });
    }

    private String resolveInitialNickname(String email, String nicknameCandidate) {
        if (nicknameCandidate != null) {
            return validateNickname(nicknameCandidate);
        }

        int delimiterIndex = email.indexOf('@');
        String defaultNickname = delimiterIndex > 0 ? email.substring(0, delimiterIndex) : email;
        if (defaultNickname.length() > NICKNAME_MAX_LENGTH) {
            return defaultNickname.substring(0, NICKNAME_MAX_LENGTH);
        }
        return defaultNickname;
    }

    private String validateNickname(String nickname) {
        String trimmed = nickname.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
        }
        if (trimmed.length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("닉네임은 " + NICKNAME_MAX_LENGTH + "자 이하여야 합니다.");
        }
        return trimmed;
    }

    private String validateLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "은(는) " + maxLength + "자 이하여야 합니다.");
        }
        return value;
    }

}


