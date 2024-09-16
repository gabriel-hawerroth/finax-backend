package br.finax.email;

import br.finax.dto.EmailDTO;
import br.finax.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component("SmtpEmailProvider")
@RequiredArgsConstructor
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender javaMailSender;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void sendMail(EmailDTO emailDTO) {
        try {
            final MimeMessage message = createMimeMessage(emailDTO);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            logger.info(() -> "Error creating mime-message: " + e.getMessage());
            throw new EmailSendingException();
        } catch (MailException e) {
            logger.info(() -> "Error sending email: " + e.getMessage());
            throw new EmailSendingException();
        } catch (Exception e) {
            logger.info(() -> "Unhandled error sending email: " + e.getMessage());
            throw new EmailSendingException();
        }
    }

    private MimeMessage createMimeMessage(EmailDTO email) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email.addressee());
        helper.setSubject(email.subject());
        helper.setText(email.content(), true);

        return message;
    }
}
