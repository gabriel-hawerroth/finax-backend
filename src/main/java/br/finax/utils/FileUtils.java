package br.finax.utils;

import br.finax.exceptions.EmptyFileException;
import br.finax.exceptions.FileCompressionErrorException;
import br.finax.exceptions.FileIOException;
import br.finax.exceptions.InvalidFileException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class FileUtils {

    private static final List<String> VALID_IMAGE_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "jfif", "webp");

    private static final int MAX_FILE_SIZE = 3 * 1024 * 1024; // 3Mb in bytes

    public static File convertByteArrayToFile(byte[] compressedFile, String fileName) throws FileIOException {
        try {
            final File tempFile = File.createTempFile(fileName, null);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(compressedFile);
            }
            return tempFile;
        } catch (IOException e) {
            throw new FileIOException("Error to create the temp file when uploading to s3");
        }
    }

    public static String getFileExtension(MultipartFile file) {
        final String fileName = Objects.requireNonNull(file.getOriginalFilename());
        final int pointIndex = fileName.lastIndexOf('.');

        if (pointIndex <= 0)
            throw new InvalidFileException("invalid file name");

        return fileName.substring(pointIndex + 1).toLowerCase();
    }

    public static byte[] compressFile(MultipartFile file) throws FileCompressionErrorException {
        return compressFile(file, false);
    }

    public static byte[] compressFile(@NonNull MultipartFile file, boolean isAttachment) throws FileCompressionErrorException {
        final String fileExtension = checkFileValidity(file);

        try {
            int width = 0;
            int height = 0;
            if (!fileExtension.equals("pdf") && !fileExtension.equals("png") && !fileExtension.equals("webp")) {
                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                width = originalImage.getWidth();
                height = originalImage.getHeight();
            }

            return switch (fileExtension) {
                case "pdf" -> compressPdf(file.getBytes());
                case "png", "webp" -> file.getBytes();
                default -> compressImage(file.getBytes(), isAttachment, new ImageSizes(width, height));
            };
        } catch (IOException e) {
            throw new InvalidFileException("error getting the file bytes");
        }
    }

    private static String checkFileValidity(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0)
            throw new EmptyFileException();

        if (file.getSize() > MAX_FILE_SIZE)
            throw new InvalidFileException("the file is too large");

        final String fileExtension = getFileExtension(file);
        if (!fileExtension.equals("pdf") && !VALID_IMAGE_EXTENSIONS.contains(fileExtension))
            throw new InvalidFileException("invalid file extension");

        return fileExtension;
    }

    private static byte[] compressImage(byte[] data, boolean isAttachment, ImageSizes imageSizes) {
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            final ImageSizes resizedImageSizes = getResizedImageSizes(imageSizes, isAttachment);

            // Resize and compress the image
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .size(resizedImageSizes.width(), resizedImageSizes.height())  // Desired size in px
                    .outputFormat("jpg")
                    .outputQuality(0.6)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new FileCompressionErrorException();
        }
    }

    private static byte[] compressPdf(byte[] pdfData) throws FileCompressionErrorException {
        try {
            try (final PDDocument document = Loader.loadPDF(pdfData)) {
                document.getDocumentCatalog().setActions(new PDDocumentCatalogAdditionalActions());

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    document.save(outputStream);
                    return outputStream.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new FileCompressionErrorException();
        }
    }

    private static ImageSizes getResizedImageSizes(ImageSizes imageSizes, boolean isAttachment) {
        final double reductionFactor = isAttachment ? 0.85 : 0.7;

        final int newWidth = (int) (imageSizes.width() * reductionFactor);
        final int newHeight = (int) (imageSizes.height() * reductionFactor);

        return new ImageSizes(newWidth, newHeight);
    }

    private record ImageSizes(
            int width,
            int height
    ) {
    }
}
