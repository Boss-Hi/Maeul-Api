package com.bosshi.maeul.user.response;

import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;

public record ProfileResponse(
        Long userId,
        String email,
        String nickname,
        String name,
        String description,
        String avatarUrl,
        int year,
        int month,
        int day
) {

    public static ProfileResponse from(Profile profile) {
        User user = profile.getUser();
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                profile.getNickname(),
                profile.getName(),
                profile.getDescription(),
                profile.getAvatarUrl(),
                profile.getYear(),
                profile.getMonth(),
                profile.getDay()
        );
    }
}

