package br.finax.controllers;

import br.finax.models.EmailDTO;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.repository.TokenRepository;
import br.finax.repository.UserRepository;
import br.finax.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.MessagingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@CrossOrigin
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    public LoginController(EmailService emailService) {
        this.emailService = emailService;
    }
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private BCryptPasswordEncoder bCrypt;

    private final EmailService emailService;

    @GetMapping("/authentication-test")
    ResponseEntity<Void> authenticationTest() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/new-user")
    ResponseEntity<User> newUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Esse usuário já existe");
        }

        user.setPassword(bCrypt.encode(user.getPassword()));
        user.setActivate(false);
        user.setAccess("premium");
        user.setCanChangePassword(false);
        user.setSignature("month");
        userRepository.save(user);

        User savedUser = userRepository.findByEmail(user.getEmail());

        Token token = new Token();
        token.setUserId(savedUser.getId());
        token.setToken(calculateHash(savedUser.getEmail()));
        tokenRepository.save(token);

        sendActivateAccountEmail(token);

        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/activate-account/{userId}/{token}")
    private ResponseEntity<Void> activaUser(@PathVariable Long userId, @PathVariable String token) {
        String savedToken = tokenRepository.findByUserId(userId).getToken();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta inexistente")
        );

        if (savedToken.equals(token)) {
            user.setActivate(true);
            userRepository.save(user);
        }

        String url = "https://hawetec.com.br/finax/ativacao-da-conta";

        UriComponentsBuilder redirectUriBuilder = UriComponentsBuilder
                .fromUriString(url);

        String redirectUrl = redirectUriBuilder.build().toUriString();

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(redirectUrl))
                .build();
    }

    @PostMapping("/send-change-password-email")
    private void requestPermissionToChangePassword(@RequestParam Long userId) {
        Token tok = tokenRepository.findByUserId(userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST)
        );

        Token token = new Token();
        if (tok != null) token.setId(tok.getId());
        token.setUserId(user.getId());
        token.setToken(calculateHash(user.getEmail()));
        tokenRepository.save(token);

        sendChangePasswordEmail(user, token.getToken());
    }

    @GetMapping("/permit-change-password/{userId}/{token}")
    private ResponseEntity<Void> permitChangePassword(@PathVariable Long userId, @PathVariable String token) {
        String savedToken = tokenRepository.findByUserId(userId).getToken();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "user not found")
        );

        if (savedToken.equals(token)) {
            user.setCanChangePassword(true);
            userRepository.save(user);
        }

        String url = "https://hawetec.com.br/finax/recuperacao-da-senha/" + user.getId();

        UriComponentsBuilder redirectUriBuilder = UriComponentsBuilder
                .fromUriString(url);

        String redirectUrl = redirectUriBuilder.build().toUriString();

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(redirectUrl))
                .build();
    }

    private static String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Converter bytes em representação hexadecimal
            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hexStringBuilder.append(String.format("%02x", b));
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private void sendActivateAccountEmail(Token token) {
        try {
            EmailDTO email = new EmailDTO();
            User user = userRepository.findById(token.getUserId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.BAD_REQUEST)
            );

            email.setDestinatario(user.getEmail());
            email.setAssunto("Ativação da conta Finax");

            String emailContent =
                    """
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
                               max-width: 42rem;
                               height: 100%;
                               margin: 0 auto;
                               padding: 0.014px 1.4rem 3rem 1.4rem;
                               font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
                                 Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue",
                                 sans-serif;
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
                                 Clique
                                 <a
                    """
                    + "href='https://api.hawetec.com.br/finax/login/activate-account/" + user.getId() + "/" + token.getToken() + "'" +
                    """
                                    target="_blank"
                                    >aqui</a
                                  >
                                  para ativar sua conta.
                                </p>
                        
                                <p class="line">
                                  Para entrar em contato com nosso suporte, envie uma mensagem nesse
                                  mesmo endereço de email, ficaremos felizes em receber sugestões de melhorias,
                                  dúvidas ou qualquer problema que você encontrar no sistema!
                                </p>
                        
                                <div class="card">
                                  <h2>Sistema em desenvolvimento</h2>
                                  <span
                                    >Informamos que nosso sistema ainda está em fase de desenvolvimento
                                    e o acesso está liberado à todos os módulos para fins de teste e
                                    divulgação.<br />
                                    <br />Após o lançamento oficial todos os usuários terão seu acesso
                                    restrito ao nível gratuito!
                                  </span>
                                </div>
                          </div>
                        </div>
                      </body>
                    </html>
                    """;

            email.setConteudo(emailContent);

            emailService.enviarEmail(email);
        } catch (MessagingException messagingException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendChangePasswordEmail(User user, String token) {
        try {
            EmailDTO email = new EmailDTO();

            email.setDestinatario(user.getEmail());
            email.setAssunto("Alteração da senha Finax");

            String emailContent =
                    """
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
                               max-width: 42rem;
                               height: 100%;
                               margin: 0 auto;
                               padding: 0.014px 1.4rem 3rem 1.4rem;
                               font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
                                 Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue",
                                 sans-serif;
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
                                 Clique
                                 <a
                    """
                        + "href='https://api.hawetec.com.br/finax/login/permit-change-password/" + user.getId() + "/" + token + "'" +
                    """
                                    target="_blank"
                                    >aqui</a
                                  >
                                  para redefinir sua senha.
                                </p>
                        
                                <p class="line">
                                  Para entrar em contato com nosso suporte, envie uma mensagem nesse
                                  mesmo endereço de email, ficaremos felizes em receber sugestões de melhorias,
                                  dúvidas ou qualquer problema que você encontrar no sistema!
                                </p>
                        
                                <div class="card">
                                  <h2>Sistema em desenvolvimento</h2>
                                  <span
                                    >Informamos que nosso sistema ainda está em fase de desenvolvimento
                                    e o acesso está liberado à todos os módulos para fins de teste e
                                    divulgação.<br />
                                    <br />Após o lançamento oficial todos os usuários terão seu acesso
                                    restrito ao nível gratuito!
                                  </span>
                                </div>
                          </div>
                        </div>
                      </body>
                    </html>
                    """;

            email.setConteudo(emailContent);

            emailService.enviarEmail(email);
        } catch (MessagingException messagingException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
