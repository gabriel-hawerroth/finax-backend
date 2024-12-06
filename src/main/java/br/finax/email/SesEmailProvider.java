package br.finax.email;

import br.finax.dto.EmailDTO;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@Component("SesEmailProvider")
public class SesEmailProvider implements EmailProvider {

    private final SesClient sesClient;

    public SesEmailProvider(@Value("${aws.iam.access-key}") String accessKey, @Value("${aws.iam.secret-key}") String secretKey) {
        final var awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        this.sesClient = SesClient.builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    @Override
    public void sendMail(EmailDTO emailDTO) {
        final String from = "Finax <noreply@appfinax.com.br>";

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
                .destination(d -> d.toAddresses(emailDTO.addressee()).build())
                .message(message)
                .source(from)
                .build();

        try {
            sesClient.sendEmail(emailRequest);
        } catch (SesException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to send email", e);
        }
    }
}
