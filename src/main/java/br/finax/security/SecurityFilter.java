package br.finax.security;

import br.finax.exceptions.UnauthorizedException;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    private final Map<String, User> tokens = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String token = this.recoverToken(request);

        if (token != null) {
            final String userMail = tokenService.validateToken(token);
            User user;

            if (userMail != null && !userMail.isEmpty()) {
                user = findTokenCache(token);

                if (user == null) {
                    user = userRepository.findByEmail(userMail)
                            .orElseThrow(UnauthorizedException::new);
                    addCache(token, user);
                }
            } else {
                removeCacheIfExists(token);
                throw new UnauthorizedException();
            }

            final var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }

    private void addCache(String token, User user) {
        // Iterate through the map to find existing entry with same user ID
        for (Map.Entry<String, User> entry : tokens.entrySet()) {
            if (Objects.equals(entry.getValue().getId(), user.getId())) {
                // Remove existing entry with same user ID
                tokens.remove(entry.getKey());
                break;
            }
        }
        // Add the new user with the token
        tokens.put(token, user);
    }

    private User findTokenCache(String token) {
        return tokens.get(token);
    }

    private void removeCacheIfExists(String token) {
        tokens.remove(token);
    }
}
