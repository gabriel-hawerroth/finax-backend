package br.finax.utils;

import br.finax.exceptions.EmptyFileException;
import br.finax.exceptions.FileCompressionErrorException;
import br.finax.exceptions.FileIOException;
import br.finax.exceptions.InvalidFileException;
import lombok.NonNull;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class FileUtils {

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

    private static final List<String> VALID_IMAGE_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "jfif", "webp");

    private static final int MAX_FILE_SIZE = 3 * 1024 * 1024; // 3Mb in bytes

    private final Logger logger = Logger.getLogger(getClass().getName());

    public static File convertByteArrayToFile(byte[] compressedFile, String fileName) {
        try {
            File tempFile = File.createTempFile(fileName, null);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(compressedFile);
            }
            return tempFile;
        } catch (IOException e) {
            throw new FileIOException("Error to create the temp file when uploading to s3");
        }
    }

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

    private String checkFileValidity(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0)
            throw new EmptyFileException();

        if (file.getSize() > MAX_FILE_SIZE)
            throw new InvalidFileException("the file is too large");

        final String fileExtension = getFileExtension(file);
        if (!fileExtension.equals("pdf") && !VALID_IMAGE_EXTENSIONS.contains(fileExtension))
            throw new InvalidFileException("invalid file extension");

        return fileExtension;
    }

    public byte[] compressFile(MultipartFile file) {
        return compressFile(file, false);
    }

    public byte[] compressFile(@NonNull MultipartFile file, boolean isAttachment) {
        final String fileExtension = checkFileValidity(file);

        try {
            return switch (fileExtension) {
                case "pdf" -> compressPdf(file.getBytes());
                case "png", "webp" -> file.getBytes();
                default -> compressImage(file.getBytes(), isAttachment);
            };
        } catch (IOException e) {
            throw new InvalidFileException("error getting the file bytes");
        }
    }

    private byte[] compressImage(byte[] data, boolean isAttachment) {
        try {
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

            logger.info(() -> "IMG - Original size: " + data.length);
            logger.info(() -> "IMG - Compressed size: " + response.length);

            return response;
        } catch (IOException e) {
            throw new FileCompressionErrorException();
        }
    }

    private byte[] compressPdf(byte[] pdfData) {
        try {
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
        } catch (IOException e) {
            throw new FileCompressionErrorException();
        }
    }

    public String getFileExtension(MultipartFile file) {
        final String fileName = Objects.requireNonNull(file.getOriginalFilename());
        final int pointIndex = fileName.lastIndexOf('.');

        if (pointIndex <= 0)
            throw new InvalidFileException("invalid file name");

        return fileName.substring(pointIndex + 1).toLowerCase();
    }
}
