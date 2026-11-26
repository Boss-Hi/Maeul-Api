package com.bosshi.maeul.gacha.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** QR 코드 문자열을 PNG 이미지로 만든다 */
@Service
public class QrImageService {

    private static final int SIZE = 320;

    public byte[] png(String code) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, SIZE, SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("QR 이미지를 만들 수 없습니다.", e);
        }
    }
}
