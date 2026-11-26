package com.bosshi.maeul.appinstall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("앱 설치 페이지")
class AppInstallTest {

    private static final String PLAY = "https://play.google.com/store/apps/details?id=a&b=1";
    private static final String STORE = "https://apps.apple.com/app/id1";
    private static final String ANDROID = "Mozilla/5.0 (Linux; Android 14; Pixel) Chrome/120";
    private static final String IPHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Safari/604";
    private static final String IPAD = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) Safari/604";
    private static final String PC = "Mozilla/5.0 (Macintosh; Intel Mac OS X) Chrome/120";
    private static final String KAKAO_ANDROID = "Mozilla/5.0 (Linux; Android 14) KAKAOTALK 10.0";
    private static final String KAKAO_IOS = "Mozilla/5.0 (iPhone) KAKAOTALK 10.0";

    @Test
    @DisplayName("기기별로 이동할 스토어를 정한다")
    void resolvesByDevice() {
        assertThat(InstallPageResolver.resolve(ANDROID, PLAY, STORE)).isEqualTo(new InstallTarget(InstallTarget.Kind.ANDROID, PLAY));
        assertThat(InstallPageResolver.resolve(IPHONE, PLAY, STORE)).isEqualTo(new InstallTarget(InstallTarget.Kind.IOS, STORE));
        assertThat(InstallPageResolver.resolve(IPAD, PLAY, STORE).kind()).isEqualTo(InstallTarget.Kind.IOS);
        assertThat(InstallPageResolver.resolve(PC, PLAY, STORE).kind()).isEqualTo(InstallTarget.Kind.OTHER);
        assertThat(InstallPageResolver.resolve(null, PLAY, STORE).kind()).isEqualTo(InstallTarget.Kind.OTHER);
    }

    @Test
    @DisplayName("인앱 브라우저는 기기보다 먼저 판별하고, 안드로이드 카카오톡만 외부 열기를 시도한다")
    void inAppBrowserWins() {
        assertThat(InstallPageResolver.resolve(KAKAO_ANDROID, PLAY, STORE).kind()).isEqualTo(InstallTarget.Kind.IN_APP);
        assertThat(InstallPageResolver.resolve(KAKAO_IOS, PLAY, STORE).kind()).isEqualTo(InstallTarget.Kind.IN_APP);
        assertThat(InstallPageResolver.isAndroidKakaoTalk(KAKAO_ANDROID)).isTrue();
        assertThat(InstallPageResolver.isAndroidKakaoTalk(KAKAO_IOS)).isFalse();
    }

    @Test
    @DisplayName("링크가 있으면 meta refresh 로 이동하고 URL 은 이스케이프한다")
    void rendersRedirectWithEscapedUrl() {
        String html = InstallPageRenderer.render(InstallPageResolver.resolve(ANDROID, PLAY, STORE), PLAY, STORE, "https://x/app", false);

        assertThat(html).contains("http-equiv=\"refresh\"").contains("id=a&amp;b=1").doesNotContain("id=a&b=1");
    }

    @Test
    @DisplayName("링크가 없는 플랫폼은 자동 이동 없이 준비 중으로 보인다")
    void rendersComingSoonWithoutLink() {
        String html = InstallPageRenderer.render(InstallPageResolver.resolve(IPHONE, PLAY, null), PLAY, null, "https://x/app", false);

        assertThat(html).doesNotContain("http-equiv").contains("준비 중");
    }

    @Test
    @DisplayName("인앱 브라우저 화면은 안드로이드 카카오톡일 때 외부 열기 링크를 넣는다")
    void rendersKakaoOpenExternal() {
        String html = InstallPageRenderer.render(InstallPageResolver.resolve(KAKAO_ANDROID, PLAY, STORE), PLAY, STORE, "https://x/app", true);

        assertThat(html).contains("kakaotalk://web/openExternal?url=https%3A%2F%2Fx%2Fapp");
    }

    @Test
    @DisplayName("스토어 URL 은 http(s) 만 쓴다")
    void acceptsOnlyHttpUrls() {
        assertThat(AppInstallController.httpUrlOrNull("javascript:alert(1)")).isNull();
        assertThat(AppInstallController.httpUrlOrNull("")).isNull();
        assertThat(AppInstallController.httpUrlOrNull(STORE)).isEqualTo(STORE);
    }
}
