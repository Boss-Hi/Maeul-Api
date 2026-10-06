package com.bosshi.maeul.user.response;

import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;

public record ProfileResponse(
        Long userId,
        String email,
        String nickname,
        String bio,
        String avatarUrl
) {

    public static ProfileResponse from(Profile profile) {
        User user = profile.getUser();
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                profile.getNickname(),
                profile.getBio(),
                profile.getAvatarUrl()
        );
    }
}

