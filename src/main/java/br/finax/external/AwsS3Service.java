package br.finax.external;

import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

@Service
public class AwsS3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String BUCKET;

    public AwsS3Service(@Value("${aws.iam.access-key}") String accessKey, @Value("${aws.iam.secret-key}") String secretKey) {
        final var awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public static String getS3FileName(long recordId, String fileExtension) {
        final long unixTimestamp = Instant.now().getEpochSecond();

        return recordId + "_" + unixTimestamp + "." + fileExtension;
    }

    public byte[] getS3File(String filename) throws ServiceException {
        try {
            final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(filename)
                    .build();

            return s3Client.getObject(getObjectRequest).readAllBytes();
        } catch (S3Exception | SdkClientException | IOException | OutOfMemoryError e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to download file from S3", e);
        }
    }

    public void uploadS3File(String filename, File file) throws ServiceException {
        try {
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(filename)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        } catch (S3Exception | SdkClientException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to upload file to S3", e);
        }
    }

    public void updateS3File(String oldFileName, String newFileName, File file) throws ServiceException {
        deleteS3File(oldFileName);
        uploadS3File(newFileName, file);
    }

    public void deleteS3File(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception | SdkClientException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to delete file from S3", e);
        }
    }
}
