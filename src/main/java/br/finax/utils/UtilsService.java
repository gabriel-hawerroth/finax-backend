package br.finax.utils;

import br.finax.exceptions.InvalidHashAlgorithmException;
import br.finax.models.User;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class UtilsService {

    private static final Map<Integer, Integer> COMPRESSION_SIZE_MAP = Map.of(
            50000, 250,     // 0,05MB - 50kb
            125000, 300,    // 0,125MB - 125kb
            250000, 400,    // 0,25MB - 250kb
            500000, 500,   // 0,5MB - 500kb
            1000000, 750,  // 1,0MB
            1500000, 1000,  // 1,5MB
            2000000, 1200,  // 2,0MB
            2500000, 1300,  // 2,5MB
            Integer.MAX_VALUE, 1500
    );

    private final Logger logger = Logger.getLogger(getClass().getName());

    private static int getImageSize(byte[] file, boolean isAttachment) {
        int size = 1500;

        for (Map.Entry<Integer, Integer> entry : COMPRESSION_SIZE_MAP.entrySet()) {
            if (file.length < entry.getKey()) {
                size = entry.getValue();
            }
        }

        if (isAttachment) {
            size = size * 2;
        }

        return size;
    }

    public byte[] compressImage(byte[] data, boolean isAttachment) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        int imageSize = getImageSize(data, isAttachment);

        // Resize and compress the image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .size(imageSize, imageSize)  // Desired size in px
                .outputFormat("jpg")
                .outputQuality(0.5)
                .toOutputStream(outputStream);

        byte[] response = outputStream.toByteArray();

        logger.info(() -> "Original size: " + data.length);
        logger.info(() -> "Compressed size: " + response.length);

        return response;
    }

    public byte[] compressPdf(byte[] pdfData) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            document.getDocumentCatalog().setActions(new PDDocumentCatalogAdditionalActions());

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                byte[] response = outputStream.toByteArray();

                logger.info(() -> "PDF - Original size: " + pdfData.length);
                logger.info(() -> "PDF - Compressed size: " + response.length);

                return response;
            }
        }
    }

    public String generateHash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new InvalidHashAlgorithmException();
        }

        byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));

        // Converter bytes em representação hexadecimal
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            hexStringBuilder.append(String.format("%02x", b));
        }

        return hexStringBuilder.toString();
    }

    public User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
