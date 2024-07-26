package br.finax.services;

import br.finax.dto.EmailDTO;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AwsEmailService {

    private final AmazonSimpleEmailService sesClient;

    public AwsEmailService(@Value("${aws.s3.access-key}") String accessKey, @Value("${aws.s3.secret-key}") String secretKey) {
        final var awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        this.sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(Regions.SA_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public void sendEmail(EmailDTO emailDTO) {
        final String from = "noreply@appfinax.com.br";

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(emailDTO.addressee()))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(emailDTO.content())))
                        .withSubject(new Content().withCharset("UTF-8").withData(emailDTO.subject()))
                )
                .withSource(from);

        sesClient.sendEmail(request);
    }
}
