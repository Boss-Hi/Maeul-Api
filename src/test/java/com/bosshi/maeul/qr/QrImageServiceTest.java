package com.bosshi.maeul.qr;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QrImageService QR 이미지 테스트")
class QrImageServiceTest {

    private final QrImageService service = new QrImageService();

    private static String decode(BufferedImage image) throws Exception {
        return new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)))).getText();
    }

    @Test
    @DisplayName("PNG 를 다시 읽으면 담은 문자열과 같고 요청한 크기다")
    void pngRoundTrip() throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(service.png("https://example.com/app?src=a-1", 400)));

        assertThat(image.getWidth()).isEqualTo(400);
        assertThat(decode(image)).isEqualTo("https://example.com/app?src=a-1");
    }

    @Test
    @DisplayName("SVG 는 viewBox 와 크기를 가진 벡터 이미지다")
    void svgStructure() {
        String svg = service.svg("GT-abc", 256);

        assertThat(svg).startsWith("<svg").contains("width=\"256\"").contains("viewBox=\"0 0 ").contains("<path");
    }

    @Test
    @DisplayName("크기는 128~1024 만 허용한다")
    void rejectsOutOfRangeSize() {
        assertThatThrownBy(() -> service.png("x", 127)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.svg("x", 1025)).isInstanceOf(IllegalArgumentException.class);
    }
}
