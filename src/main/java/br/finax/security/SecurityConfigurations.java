package br.finax.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;

    @Value("${finax.security.cookie-secure:false}")
    private boolean secureCookie;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        final var csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
            .path("/")
                .sameSite("Lax")
                .secure(secureCookie)
                .maxAge(600)  // Token CSRF válido por 10 minutos, depois força refresh
        );

        // SpaCsrfTokenRequestHandler resolve o conflito entre:
        // 1. BREACH protection (XOR na resposta via handle())
        // 2. Angular enviando valor bruto do cookie no header X-XSRF-TOKEN (sem XOR em resolveCsrfTokenValue)
        //
        // Fluxo:
        // - Token via header X-XSRF-TOKEN → resolveCsrfTokenValue() retorna valor bruto (sem XOR)
        // - Token via parâmetro _csrf → resolveCsrfTokenValue() retorna valor XOR/Base64
        // - handle() sempre aplica XOR para BREACH protection na geração do token
        return httpSecurity
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .ignoringRequestMatchers(
                            "/auth/csrf",
                            "/login/password-recovery/validate",
                            "/login/password-recovery/confirm"
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        final PasswordEncoder bcrypt = new BCryptPasswordEncoder();
        final PasswordEncoder delegating = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }

                final String normalized = encodedPassword.trim();

                if (normalized.startsWith("{")) {
                    try {
                        return delegating.matches(rawPassword, normalized);
                    } catch (IllegalArgumentException ignored) {
                        // Fallback to bcrypt for legacy values missing a valid prefix mapping.
                    }
                }

                return bcrypt.matches(rawPassword, normalized);
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }

                final String normalized = encodedPassword.trim();

                if (normalized.startsWith("{")) {
                    try {
                        return delegating.upgradeEncoding(normalized);
                    } catch (IllegalArgumentException ignored) {
                        return false;
                    }
                }

                return bcrypt.upgradeEncoding(normalized);
            }
        };
    }
}
