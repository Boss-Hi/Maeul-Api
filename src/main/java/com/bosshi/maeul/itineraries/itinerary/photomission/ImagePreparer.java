package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.ai.AiImage;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/** AI 로 보낼 이미지를 준비한다: 긴 변 1024px 로 줄이고, EXIF 촬영 시각을 읽는다 */
final class ImagePreparer {

    static final int MAX_SIDE = 1024;

    private ImagePreparer() {
    }

    /** JPEG, PNG 는 긴 변을 줄여 JPEG 로 보낸다. WebP 는 읽을 수 있는 코덱이 없어 그대로 보낸다 */
    static AiImage prepare(byte[] data, String mimeType) {
        if ("image/webp".equals(mimeType)) {
            return new AiImage(data, mimeType);
        }
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(data));
            if (source == null) {
                throw new IllegalArgumentException("사진을 읽을 수 없습니다.");
            }
            double scale = Math.min(1.0, (double) MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
            int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = rgb.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, width, height, java.awt.Color.WHITE, null);
            graphics.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(rgb, "jpg", out);
            return new AiImage(out.toByteArray(), "image/jpeg");
        } catch (IOException e) {
            throw new IllegalArgumentException("사진을 읽을 수 없습니다.", e);
        }
    }

    /** EXIF 촬영 시각(없으면 empty). EXIF 시각에는 시간대가 없어 서비스 시간대로 본다 */
    static Optional<LocalDateTime> takenAt(byte[] data, ZoneId zone) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(data));
            ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exif == null) {
                return Optional.empty();
            }
            Date taken = exif.getDateOriginal(TimeZone.getTimeZone(zone));
            return taken == null ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(taken.toInstant(), zone));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
