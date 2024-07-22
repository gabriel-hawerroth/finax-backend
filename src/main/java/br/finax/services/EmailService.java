package br.finax.services;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.finax.dto.EmailDTO;
import br.finax.dto.HunterResponse;
import br.finax.enums.EmailType;
import br.finax.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final String apiUrl;

    private final JavaMailSender javaMailSender;

    private final String apiKey;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public EmailService(JavaMailSender javaMailSender, Environment environment, @Value("${hunter.api.key}") String apiKey) {
        this.javaMailSender = javaMailSender;

        if (environment.getActiveProfiles().length > 0 && Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            apiUrl = "http://localhost:8080";
        } else {
            apiUrl = "https://apifinax.hawetec.com.br";
        }

        this.apiKey = apiKey;
    }

    public boolean verifyEmail(String email) {
        final ResponseEntity<HunterResponse> response;
        try {
            response = RestClient.create().get()
                    .uri("https://api.hunter.io/v2/email-verifier?email=" + email + "&api_key=" + apiKey)
                    .retrieve()
                    .toEntity(HunterResponse.class);
        } catch (Exception e) {
            logger.info("Email verification failed: " + e.getMessage());
            return true;
        }

        if (!response.getStatusCode().is2xxSuccessful())
            return true;

        final var responseBody = response.getBody();

        if (responseBody == null || responseBody.data() == null || responseBody.data().result() == null)
            return true;

        return responseBody.data().result().equals("deliverable");
    }

    public void sendMail(EmailDTO email) {
        try (final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            final Future<?> future = executorService.submit(() -> {
                try {
                    final MimeMessage message = createMimeMessage(email);

                    javaMailSender.send(message);
                } catch (MessagingException e) {
                    throw new EmailSendingException();
                }
            });

            try {
                future.get();
            } catch (EmailSendingException | ExecutionException | InterruptedException e) {
                future.cancel(true);
                throw new EmailSendingException();
            }
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

    public String buildEmailTemplate(EmailType emailType, Long userId, String token) {
        final String url = apiUrl + "/login/" + emailType.getValue() + "/" + userId + "/" + token;

        final String action = switch (emailType) {
            case ACTIVATE_ACCOUNT -> " ativar sua conta.";
            case CHANGE_PASSWORD -> " redefinir sua senha.";
        };

        return """
                <!DOCTYPE html>
                <html lang="pt-br">
                    <head>
                        <meta charset="UTF-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                        <title>Document</title>
                        <style>
                            html,
                            body {
                                width: 100%;
                                height: 100%;
                            }

                            * {
                                color: #000;
                            }

                            .email {
                                width: 100%;
                                height: 100%;
                            }

                            .box {
                                width: 100%;
                                max-width: 44rem;
                                height: 100%;
                                margin: 0 auto;
                                padding: 0.014px 1.5rem 3rem 1.5rem;
                                font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
                                  Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
                                font-size: 1.1rem;
                                background-color: #eff3f8;
                            }

                            h1 {
                                width: 100%;
                                text-align: center;
                                font-size: 3.6rem;
                                color: #009465;
                                margin-bottom: 2.5rem;
                            }

                            a {
                                color: #000;
                            }

                            .line {
                                margin-bottom: 2rem;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="email">
                            <div class="box">
                                <h1>Finax</h1>

                                <p class="line">
                                    Clique <a href='
                """
                + url +
                """
                                        ' target="_blank">aqui</a>
                                        para
                        """
                + action +
                """
                                        </p>

                                        <p class="line">
                                            Para entrar em contato com nosso suporte, envie uma mensagem nesse
                                            mesmo endereço de email, ficaremos felizes em receber sugestões de melhorias,
                                            dúvidas ou qualquer problema que você encontrar no sistema!
                                        </p>
                                    </div>
                                </div>
                            </body>
                        </html>
                        """;
    }
}
