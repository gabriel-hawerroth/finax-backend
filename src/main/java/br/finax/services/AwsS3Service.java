package br.finax.services;

import br.finax.enums.ErrorCategory;
import br.finax.enums.S3FolderPath;
import br.finax.exceptions.ServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

@Service
public class AwsS3Service {

    private static final String BUCKET = "finax";

    private final AmazonS3 s3Client;

    public AwsS3Service(@Value("${aws.s3.access-key}") String accessKey, @Value("${aws.s3.secret-key}") String secretKey) {
        final var awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.SA_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public byte[] getS3File(String filename) {
        final S3Object s3Object = s3Client.getObject(BUCKET, filename);
        try (S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(objectInputStream);
        } catch (IOException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to download file from S3", e);
        }
    }

    public void uploadS3File(String filename, File file) {
        s3Client.putObject(BUCKET, filename, file);
    }

    public void updateS3File(String oldFileName, String newFileName, File file) {
        s3Client.deleteObject(BUCKET, oldFileName);
        s3Client.putObject(BUCKET, newFileName, file);
    }

    public void deleteS3File(String fileName) {
        s3Client.deleteObject(BUCKET, fileName);
    }

    public String getS3FileName(long recordId, String fileExtension, S3FolderPath s3FolderPath) {
        final String folderPath = s3FolderPath.getPath();
        final long unixTimestamp = Instant.now().getEpochSecond();

        return folderPath + recordId + "_" + unixTimestamp + "." + fileExtension;
    }
}
