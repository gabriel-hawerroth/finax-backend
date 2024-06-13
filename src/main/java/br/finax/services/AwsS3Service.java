package br.finax.services;

import br.finax.enums.S3FolderPath;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

        return STR."\{folderPath}\{recordId}_\{unixTimestamp}.\{fileExtension}";
    }
}
