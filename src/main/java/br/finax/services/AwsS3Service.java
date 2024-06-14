package br.finax.services;

import br.finax.enums.S3FolderPath;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

@Service
public class AwsS3Service {

    private static final String BUCKET = "finax";

    private AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(
                System.getenv("AWS_S3_ACCESS_KEY"),
                System.getenv("AWS_S3_SECRET_KEY")
        );
    }

    private AmazonS3 awsS3ClientBuilder() {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                .withRegion(Regions.SA_EAST_1)
                .build();
    }

    public byte[] getS3File(String filename) {
        final AmazonS3 s3Client = awsS3ClientBuilder();

        final GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET, filename);

        try (InputStream inputStream = s3Client.getObject(getObjectRequest).getObjectContent();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            // Trate a exceção conforme necessário
            throw new RuntimeException();
        }
    }

    public void uploadS3File(String filename, File file) {
        final AmazonS3 s3Client = awsS3ClientBuilder();

        s3Client.putObject(BUCKET, filename, file);
    }

    public void updateS3File(String oldFileName, String newFileName, MultipartFile file) {
        final AmazonS3 s3Client = awsS3ClientBuilder();

        s3Client.deleteObject(BUCKET, oldFileName);
        s3Client.putObject(BUCKET, newFileName, (File) file);
    }

    public void deleteS3File(String fileName) {
        final AmazonS3 s3Client = awsS3ClientBuilder();

        s3Client.deleteObject(BUCKET, fileName);
    }

    public String getS3FileName(long recordId, String fileExtension, S3FolderPath s3FolderPath) {
        final String folderPath = s3FolderPath.getPath();
        final long unixTimestamp = Instant.now().getEpochSecond();

        return folderPath.concat(String.valueOf(recordId)).concat(String.valueOf(unixTimestamp)).concat(fileExtension);
    }
}
