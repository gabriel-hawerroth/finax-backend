package br.finax.security;

import br.finax.models.AccessLog;
import br.finax.models.User;
import br.finax.repository.AccessLogRepository;
import br.finax.repository.UserRepository;

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
    private UserRepository userRepository;
    @Autowired
    private AccessLogRepository accessLogRepository;

    @Override
    public Authentication authenticate(Authentication sendedCredentials) throws AuthenticationException {
        final Authentication authentication = this.doLogin(
                sendedCredentials.getPrincipal().toString(),
                sendedCredentials.getCredentials().toString()
        );

        ((AbstractAuthenticationToken) authentication).setDetails(authentication.getDetails());

        return authentication;
    }

    private Authentication doLogin(String username, String password) {
        final User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!encoder.matches(password, user.getPassword()))
            throw new BadCredentialsException("Bad credentials");

        if (!user.isActive())
            throw new BadCredentialsException("Inactive user");

        if (user.getId() != 1 && user.getId() != 2) {
            AccessLog log = new AccessLog(user.getId(), LocalDateTime.now());
            accessLogRepository.save(log);
        }

        return new UsernamePasswordAuthenticationToken(user, null, null);
    }
}
