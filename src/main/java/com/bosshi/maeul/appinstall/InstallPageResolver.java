package com.bosshi.maeul.appinstall;

import java.util.Locale;

/** User-Agent 로 앱 설치 페이지의 화면을 정한다 */
final class InstallPageResolver {

    private InstallPageResolver() {
    }

    static InstallTarget resolve(String userAgent, String androidUrl, String iosUrl) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);
        if (isInApp(ua)) {
            return new InstallTarget(InstallTarget.Kind.IN_APP, null);
        }
        if (ua.contains("android")) {
            return new InstallTarget(InstallTarget.Kind.ANDROID, androidUrl);
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return new InstallTarget(InstallTarget.Kind.IOS, iosUrl);
        }
        return new InstallTarget(InstallTarget.Kind.OTHER, null);
    }

    static boolean isAndroidKakaoTalk(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);
        return ua.contains("kakaotalk") && ua.contains("android");
    }

    private static boolean isInApp(String ua) {
        return ua.contains("kakaotalk") || ua.contains("naver(inapp") || ua.contains("line/") || ua.contains("instagram")
                || ua.contains("fban") || ua.contains("fbav");
    }
}
