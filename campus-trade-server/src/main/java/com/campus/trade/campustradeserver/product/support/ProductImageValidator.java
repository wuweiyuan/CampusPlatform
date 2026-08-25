package com.campus.trade.campustradeserver.product.support;

import com.campus.trade.campustradeserver.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
@Component
public class ProductImageValidator {
    private static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;

    private static final int MAX_BASE64_LENGTH =
            ((MAX_IMAGE_BYTES + 2) / 3) * 4;

    private static final Pattern DATA_URL_PATTERN = Pattern.compile(
            "^data:(image/(?:jpeg|png|webp));base64,([A-Za-z0-9+/]+={0,2})$"
    );

    @PostConstruct
    void registerImageReaders() {
        ImageIO.scanForPlugins();
    }

    public void validate(String imageBase64) {
        if (imageBase64 == null) {
            return;
        }

        Matcher matcher = DATA_URL_PATTERN.matcher(imageBase64);
        if (!matcher.matches()) {
            throw invalidImage();
        }

        String encodedContent = matcher.group(2);
        String declaredMimeType = matcher.group(1);
        if (encodedContent.length() > MAX_BASE64_LENGTH) {
            throw invalidImage();
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(encodedContent);
        } catch (IllegalArgumentException exception) {
            throw invalidImage();
        }

        if (imageBytes.length > MAX_IMAGE_BYTES) {
            throw invalidImage();
        }
        validateActualImage(declaredMimeType, imageBytes);

    }

    private void validateActualImage(String declaredMimeType, byte[] imageBytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(imageBytes)
        )) {
            if (input == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);

                String actualMimeType = toMimeType(reader.getFormatName());
                if (!declaredMimeType.equals(actualMimeType)) {
                    throw invalidImage();
                }

                reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalidImage();
        }
    }
    private String toMimeType(String formatName) {
        return switch (formatName.toLowerCase(Locale.ROOT)) {
            case "jpeg", "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> throw invalidImage();
        };
    }
    private BusinessException invalidImage() {
        return new BusinessException(3004, "商品图片不合法");
    }
}
