package com.bosshi.maeul.gacha.service;

import java.security.SecureRandom;
import java.util.Base64;

/** QR 에 담는 1회용 코드. 가챠권과 상품 QR 을 서로 바꿔 스캔하지 않도록 접두사로 구분한다 */
final class QrCodes {

    static final String TICKET_PREFIX = "GT-";
    static final String PRIZE_PREFIX = "GP-";

    private static final SecureRandom RANDOM = new SecureRandom();

    private QrCodes() {
    }

    static String newCode(String prefix) {
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static boolean hasPrefix(String code, String prefix) {
        return code != null && code.startsWith(prefix);
    }
}
