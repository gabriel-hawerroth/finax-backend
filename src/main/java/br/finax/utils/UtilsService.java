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

@Service
public class UtilsService {
    public static byte[] compressImage(byte[] data, boolean isAttachment) throws IOException {
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

        System.out.println("Original size: " + data.length + " bytes");
        System.out.println("Compressed size: " + response.length + " bytes");

        return response;
    }

    private static int getImageSize(byte[] data, boolean isAttachment) {
        int imageSize =
                data.length < 50000 ? 250 : //0,05MB - 50kb
                        data.length < 125000 ? 300 : //0,125MB - 125kb
                                data.length < 250000 ? 400 : //0,25MB - 250kb
                                        data.length < 500000 ? 500 : //0,5MB - 500kb
                                                data.length < 1000000 ? 750 : //1,0MB
                                                        data.length < 1500000 ? 1000 : //1,5MB
                                                                data.length < 2000000 ? 1200 : //2,0MB
                                                                        data.length < 2500000 ? 1300 : //2,5MB
                                                                                data.length < 3000000 ? 1400 : //3,0MB
                                                                                        data.length < 3500000 ? 1500 : //3,5MB
                                                                                                1600;

        if (isAttachment) {
            imageSize = imageSize * 2;
        }
        return imageSize;
    }

    public static byte[] compressPdf(byte[] pdfData) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            document.getDocumentCatalog().setActions(new PDDocumentCatalogAdditionalActions());

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                byte[] response = outputStream.toByteArray();
                System.out.println("PDF - Original size: " + pdfData.length + " bytes");
                System.out.println("PDF - Compressed size: " + response.length + " bytes");
                return response;
            }
        }
    }

    public static String generateHash(String text) {
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
