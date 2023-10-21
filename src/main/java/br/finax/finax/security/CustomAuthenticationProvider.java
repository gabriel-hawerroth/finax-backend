package br.finax.finax.security;

import br.finax.finax.models.AccessLog;
import br.finax.finax.models.User;
import br.finax.finax.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Primary
@Component
public class CustomAuthenticationProvider implements AuthenticationManager {

    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private br.finax.finax.repository.UserRepository userRepository;
    @Autowired
    private AccessLogRepository accessLogRepository;

    @Override
    public Authentication authenticate(Authentication sendedCredentials) throws AuthenticationException {
        String username = sendedCredentials.getPrincipal().toString();
        String password = sendedCredentials.getCredentials().toString();

        Authentication authentication = this.fazerLogin(username, password);
        if (authentication == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        ((AbstractAuthenticationToken) authentication).setDetails(authentication.getDetails());

        if (!username.equals("gabrielhawerroth04@gmail.com")) {
            User user = userRepository.findByEmail(username);
            AccessLog log = new AccessLog(user.getId(), user.getEmail(), LocalDateTime.now());
            accessLogRepository.save(log);
        }

        return authentication;
    }

    private Authentication fazerLogin(String username, String password) {
        User loginExistente = userRepository.findByEmail(username);
        if (loginExistente == null) {
            return null;
        }
        if (!encoder.matches(password, loginExistente.getPassword())){
            return null;
        }

        UserDetailsCustom userDetailsCustom = new UserDetailsCustom(username, password);
        return new UsernamePasswordAuthenticationToken(userDetailsCustom, null, null);
    }

}
