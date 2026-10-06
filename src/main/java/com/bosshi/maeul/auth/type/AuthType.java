package com.bosshi.maeul.auth.type;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public enum AuthType {
    KAKAO,
    BASIC,
    GOOGLE,
    APPLE
}
