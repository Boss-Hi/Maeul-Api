package com.bosshi.maeul.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * QR 코드 문자열을 PNG 또는 SVG 이미지로 만든다. 무엇을 담을지는 부르는 쪽이 정한다.
 * 여백은 4칸, 오류 정정은 M 으로 고정하고 로고는 넣지 않는다.
 */
@NamedInterface
@Service
public class QrImageService {

    public static final int DEFAULT_SIZE = 320;
    public static final int MIN_SIZE = 128;
    public static final int MAX_SIZE = 1024;

    private static final Map<EncodeHintType, Object> HINTS = Map.of(
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN, 4,
            EncodeHintType.CHARACTER_SET, "UTF-8");

    public byte[] png(String content, int size) {
        requireValidSize(size);
        BitMatrix matrix = encode(content, size);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("QR 이미지를 만들 수 없습니다.", e);
        }
    }

    public String svg(String content, int size) {
        requireValidSize(size);
        BitMatrix matrix = encode(content, 1);
        int modules = matrix.getWidth();
        StringBuilder path = new StringBuilder();
        for (int y = 0; y < modules; y++) {
            for (int x = 0; x < modules; x++) {
                if (matrix.get(x, y)) {
                    path.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
                }
            }
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + size + "\" height=\"" + size + "\" viewBox=\"0 0 "
                + modules + " " + modules + "\" shape-rendering=\"crispEdges\"><rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>"
                + "<path fill=\"#000\" d=\"" + path + "\"/></svg>";
    }

    private static BitMatrix encode(String content, int size) {
        try {
            return new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, HINTS);
        } catch (WriterException e) {
            throw new IllegalStateException("QR 이미지를 만들 수 없습니다.", e);
        }
    }

    private static void requireValidSize(int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException("size 는 " + MIN_SIZE + "~" + MAX_SIZE + " 이어야 합니다.");
        }
    }
}
