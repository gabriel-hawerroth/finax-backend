package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendMail(EmailDTO email) throws MessagingException {
        final MimeMessage message = javaMailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email.addressee());
        helper.setSubject(email.subject());
        helper.setText(email.content(), true);

        javaMailSender.send(message);
    }

    public String buildEmailTemplate(EmailType emailType, Long userId, String token) {
        final String url = "https://api.hawetec.com.br/finax/login/" + emailType.getValue() + "/" + userId + "/" + token;

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
                             Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue",
                             sans-serif;
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
