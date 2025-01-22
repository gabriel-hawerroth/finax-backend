package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.dto.HunterResponse;
import br.finax.email.EmailProvider;
import br.finax.enums.EmailType;
import br.finax.external.HunterIoService;
import br.finax.models.User;
import br.finax.utils.ServiceUrls;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class EmailService {

    private final HunterIoService hunterIoService;
    private final ServiceUrls serviceUrls;

    private final EmailProvider emailProvider;

    private final Logger logger = Logger.getLogger(EmailService.class.getName());

    public EmailService(
            @Value("${finax.email.provider}") String emailProvider, Map<String, EmailProvider> emailProvidersMap,
            HunterIoService hunterIoService, ServiceUrls serviceUrls
    ) {
        this.hunterIoService = hunterIoService;
        this.serviceUrls = serviceUrls;

        this.emailProvider = emailProvidersMap.get(emailProvider);
    }

    public boolean verifyEmail(String email) {
        final ResponseEntity<HunterResponse> response;
        try {
            response = hunterIoService.sendEmailVerifier(email);
        } catch (Exception e) {
            logger.info("Email verification failed: " + e.getMessage());
            return true;
        }

        if (!response.getStatusCode().is2xxSuccessful())
            return true;

        final var responseBody = response.getBody();

        if (responseBody == null || responseBody.data() == null || responseBody.data().result() == null)
            return true;

        var result = responseBody.data().result();
        return result.equals(AcceptedEmailVerificationResults.DELIVERABLE.value) || result.equals(AcceptedEmailVerificationResults.RISKY.value);
    }

    public void sendMail(EmailDTO emailDTO) {
        emailProvider.sendMail(emailDTO);
    }

    public String buildEmailContent(@NonNull EmailType emailType, User user, String token) {
        if (emailType == EmailType.SYSTEM_UPDATE) {
            return String.format("""
                    Olá, %s!
                    <br>
                    Temos uma ótima notícia para você: acabamos de liberar uma nova versão com uma novidade incrível!
                    <br><br>
                    
                    Agora é possível criar e gerenciar subcontas!
                    <br>
                    Acesse a tela "Minhas contas" no sistema e confira todas as possibilidades dessa nova funcionalidade.
                    <br><br>
                    
                    Além disso, implementamos outras melhorias gerais e corrigimos alguns bugs para tornar sua experiência ainda melhor.
                    <br>
                    
                    Estamos sempre trabalhando para oferecer o melhor para você!
                    """, user.getFirstName());
        }

        final String url = serviceUrls.getApiUrl() + "/login/" + emailType.getValue() + "/" + user.getId() + "/" + token;

        final String action = switch (emailType) {
            case ACTIVATE_ACCOUNT -> " ativar sua conta.";
            case CHANGE_PASSWORD -> " redefinir sua senha.";
            default -> "";
        };

        return String.format("Clique <a id=\"link\" href='%s' target=\"_blank\">aqui</a> para %s", url, action);
    }

    public String buildEmailTemplate(@NonNull String mailContent) {
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
                                color: blue;
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
                """
                + mailContent +
                """
                                        <br>
                                        Este link é válido por apenas 2 horas.
                                        </p>
                        
                                        <p class="line">
                                            Para entrar em contato com nosso suporte, envie uma mensagem para
                                            suporte@appfinax.com.br, ficaremos felizes em receber sugestões de melhorias,
                                            dúvidas ou qualquer problema que você encontrar no sistema!
                                            <br><br>
                        
                                            Abraços,
                                            <br>
                                            Equipe Finax
                                        </p>
                                    </div>
                                </div>
                            </body>
                        </html>
                        """;
    }

    public String buildCancelAccountEmailTemplate(long userId, @NonNull String token) {
        final String url = serviceUrls.getApiUrl() + "/login/cancel-user/" + userId + "/" + token;

        return """
                <!DOCTYPE html>
                <html lang="pt-br">
                    <head>
                        <meta charset="UTF-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                        <title>Confirmação de Cancelamento da Conta Finax</title>
                        <style>
                            html, body {
                                width: 100%;
                                height: 100%;
                                margin: 0;
                                padding: 0;
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
                                color: blue;
                            }
                
                            .line {
                                margin-bottom: 2rem;
                            }
                
                            .important {
                                font-weight: bold;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="email">
                            <div class="box">
                                <h1>Finax</h1>
                
                                <p class="line">
                                    Você solicitou o cancelamento da sua conta Finax. Antes de prosseguir, queremos lembrar que esta ação é <span class="important">irreversível</span>. A confirmação do cancelamento resultará na exclusão permanente de todos os seus dados, incluindo:
                                </p>
                
                                <ul class="line">
                                    <li><span class="important">Assinatura e benefícios</span>: Sua assinatura será cancelada imediatamente, e você perderá acesso a todos os benefícios exclusivos.</li>
                                    <li><span class="important">Dados de conta</span>: Todas as suas informações pessoais e dados armazenados serão permanentemente deletados do nosso sistema.</li>
                                    <li><span class="important">Histórico de uso</span>: Seu histórico de uso, preferências e configurações personalizadas também serão removidos.</li>
                                </ul>
                
                                <p class="line">
                                    Clique <a id="link" href="
                """
                + url +
                """
                                            " target="_blank">aqui</a> para confirmar o cancelamento da sua conta.
                                            <br>
                                            Este link é válido por apenas 2 horas.
                                        </p>
                        
                                        <p class="line">
                                            Se você está enfrentando algum problema ou se há algo que possamos fazer para melhorar sua experiência, por favor, entre em contato conosco antes de confirmar o cancelamento. Estamos comprometidos em fornecer o melhor serviço possível e ficaremos felizes em ajudar a resolver qualquer questão que você tenha.
                                        </p>
                        
                                        <p class="line">
                                            Para entrar em contato com nosso suporte, envie uma mensagem para suporte@appfinax.com.br. Nossa equipe está pronta para atender suas dúvidas, sugestões ou preocupações.
                                        </p>
                        
                                        <p class="line">
                                            Obrigado por escolher a Finax. Esperamos que reconsidere e continue a fazer parte da nossa comunidade.
                                        </p>
                                    </div>
                                </div>
                            </body>
                        </html>
                        """;
    }

    private enum AcceptedEmailVerificationResults {
        DELIVERABLE,
        RISKY;

        private final String value;

        AcceptedEmailVerificationResults() {
            this.value = this.name().toLowerCase();
        }
    }
}
