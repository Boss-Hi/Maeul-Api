package com.bosshi.maeul.appinstall;

import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/** 앱 설치 안내 HTML 한 장. 외부 리소스 없이 만든다 */
final class InstallPageRenderer {

    private static final String NAME = "Maeul";

    private InstallPageRenderer() {
    }

    static String render(InstallTarget target, String androidUrl, String iosUrl, String pageUrl, boolean androidKakaoTalk) {
        StringBuilder head = new StringBuilder();
        StringBuilder body = new StringBuilder();
        body.append("<h1>").append(NAME).append("</h1>");
        switch (target.kind()) {
            case ANDROID, IOS -> {
                if (target.redirectUrl() == null) {
                    body.append("<p>앱 준비 중입니다. 조금만 기다려 주세요.</p>");
                } else {
                    head.append("<meta http-equiv=\"refresh\" content=\"2;url=").append(esc(target.redirectUrl())).append("\">");
                    body.append("<p>앱을 설치하러 이동합니다.</p>");
                    body.append(button(target.kind() == InstallTarget.Kind.ANDROID ? "Google Play" : "App Store", target.redirectUrl()));
                }
            }
            case IN_APP -> {
                body.append("<p>기본 브라우저(Chrome, Safari)로 열어 주세요.</p>");
                if (androidKakaoTalk) {
                    String external = "kakaotalk://web/openExternal?url=" + UriUtils.encode(pageUrl, StandardCharsets.UTF_8);
                    body.append("<a class=\"btn\" href=\"").append(esc(external)).append("\">기본 브라우저로 열기</a>");
                }
                body.append(storeButtons(androidUrl, iosUrl));
            }
            case OTHER -> {
                body.append("<p>휴대폰으로 QR을 다시 스캔하거나 아래 스토어로 이동하세요.</p>");
                body.append(storeButtons(androidUrl, iosUrl));
            }
        }
        return "<!doctype html><html lang=\"ko\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + NAME + " 앱 설치</title>" + head
                + "<style>body{font-family:sans-serif;text-align:center;margin:0;padding:48px 24px}"
                + ".btn{display:block;max-width:280px;margin:12px auto;padding:14px;border-radius:10px;background:#222;color:#fff;text-decoration:none}"
                + ".off{background:#bbb}</style></head><body>" + body + "</body></html>";
    }

    private static String storeButtons(String androidUrl, String iosUrl) {
        return button("Google Play", androidUrl) + button("App Store", iosUrl);
    }

    private static String button(String label, String url) {
        return url == null
                ? "<span class=\"btn off\">" + label + " 준비 중</span>"
                : "<a class=\"btn\" href=\"" + esc(url) + "\">" + label + "</a>";
    }

    private static String esc(String value) {
        return HtmlUtils.htmlEscape(value);
    }
}
