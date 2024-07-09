package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class EmailService {

    private final String API_URL;

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender, Environment environment) {
        this.javaMailSender = javaMailSender;

        if (environment.getActiveProfiles().length > 0 && Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            API_URL = "http://localhost:8080";
        } else {
            API_URL = "https://apifinax.hawetec.com.br";
        }
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
        final String url = API_URL + "/login/" + emailType.getValue() + "/" + userId + "/" + token;

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

                            .card {
                                width: 95%;
                                max-width: 29rem;
                                border: 1px solid #00000053;
                                padding: 1rem;
                                margin: 4rem auto;
                                border-radius: 10px;
                            }

                            h2 {
                                color: #2b2b2b;
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

                                        <div class="card">
                                            <h2>Sistema em desenvolvimento</h2>

                                            <span>
                                                Informamos que nosso sistema ainda está em fase de desenvolvimento
                                                e o acesso está liberado à todos os módulos para fins de teste e
                                                divulgação.
                                                <br /> <br />
                                                Após o lançamento oficial todos os usuários terão seu acesso
                                                restrito ao nível gratuito!
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </body>
                        </html>
                        """;
    }
}
