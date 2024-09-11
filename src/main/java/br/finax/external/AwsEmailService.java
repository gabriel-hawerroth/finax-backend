package br.finax.external;

import br.finax.dto.EmailDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class AwsEmailService {

    private final SesClient sesClient;

    public AwsEmailService(@Value("${aws.s3.access-key}") String accessKey, @Value("${aws.s3.secret-key}") String secretKey) {
        final var awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        this.sesClient = SesClient.builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public void sendMail(EmailDTO emailDTO) {
        final String from = "noreply@appfinax.com.br";

        final Content subjectContent = Content.builder()
                .data(emailDTO.subject())
                .charset("UTF-8")
                .build();

        final Content bodyContent = Content.builder()
                .data(emailDTO.content())
                .charset("UTF-8")
                .build();

        final Body body = Body.builder()
                .html(bodyContent)
                .build();

        final Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();

        final SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(Destination.builder()
                        .toAddresses(emailDTO.addressee())
                        .build())
                .message(message)
                .source(from)
                .build();

        try {
            sesClient.sendEmail(emailRequest);
        } catch (SesException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
