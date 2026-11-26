package com.bosshi.maeul.appinstall;

import com.bosshi.maeul.qr.QrImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("앱 설치 QR")
class AppInstallQrControllerTest {

    private static AppInstallQrController controller(String base) {
        return new AppInstallQrController(new QrImageService(), base);
    }

    @Test
    @DisplayName("고정 주소 /app 에 src 를 붙인다")
    void buildsFixedUrl() {
        assertThat(controller("https://maeul.kr/").url("shop_1")).isEqualTo("https://maeul.kr/app?src=shop_1");
        assertThat(controller("https://maeul.kr").url(null)).isEqualTo("https://maeul.kr/app");
    }

    @Test
    @DisplayName("도메인이 없거나 src 가 허용 문자가 아니면 실패한다")
    void rejectsMissingBaseOrBadSrc() {
        assertThatThrownBy(() -> controller("").url("a")).hasMessageContaining("APP_PUBLIC_BASE_URL");
        assertThatThrownBy(() -> controller("https://maeul.kr").url("a b&x=1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> controller("https://maeul.kr").url("a".repeat(33))).isInstanceOf(IllegalArgumentException.class);
    }
}
