package com.bosshi.maeul.appinstall;

/**
 * 앱 설치 페이지가 보여 줄 화면.
 *
 * @param redirectUrl 자동 이동할 스토어 URL. 없으면 자동 이동하지 않는다
 */
record InstallTarget(Kind kind, String redirectUrl) {

    enum Kind {
        /** 모바일 기기: 해당 스토어로 자동 이동 */
        ANDROID, IOS,
        /** 인앱 브라우저: 기본 브라우저로 열기 안내 */
        IN_APP,
        /** PC 등: 두 스토어 버튼 */
        OTHER
    }
}
