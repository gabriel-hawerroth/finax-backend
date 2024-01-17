package br.finax.utils;

import br.finax.enums.ImgFormat;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;

@Service
public class UtilsService {
    public byte[] compressImage(byte[] data, boolean isAttachment, ImgFormat imgFormat) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        int imageSize = getImageSize(data, isAttachment);

        // Resize and compress the image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .size(imageSize, imageSize)  // Desired size in px
                .outputFormat(imgFormat.getImgFormat())
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

    public byte[] compressPdf(byte[] pdfData) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            // Remove unused resources and optimize
            document.setAllSecurityToBeRemoved(true);
            document.getDocumentCatalog().setActions(new PDDocumentCatalogAdditionalActions());
            removeUnusedObjects(document);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.getDocument().setIsXRefStream(true);
                document.getDocumentCatalog().setStructureTreeRoot(null);
                document.save(outputStream);

                byte[] response = outputStream.toByteArray();

                System.out.println("PDF - Original size: " + pdfData.length + " bytes");
                System.out.println("PDF - Compressed size: " + response.length + " bytes");

                return outputStream.toByteArray();
            }
        }
    }

    // Removes unused objects from pdf
    private void removeUnusedObjects(PDDocument document) {
        PDPageTree pages = document.getPages();
        for (PDPage page : pages) {
            PDResources resources = page.getResources();
            if (resources != null) {
                resources.getCOSObject().setDirect(false);
                resources.getCOSObject().setNeedToBeUpdated(true);
            }
        }

        PDDocumentCatalog catalog = document.getDocumentCatalog();
        catalog.getCOSObject().setDirect(false);
        catalog.getCOSObject().setNeedToBeUpdated(true);
    }
}
