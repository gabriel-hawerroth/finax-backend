package br.finax.security;

import br.finax.enums.ErrorCategory;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final Map<String, User> usersCache = new ConcurrentHashMap<>();

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String token = this.recoverToken(request);

        if (token != null) {
            final String userMail = tokenService.validateToken(token);
            if (userMail == null)
                throw new ServiceException(ErrorCategory.UNAUTHORIZED, "token expired or invalid");

            final User user = findUserByMail(userMail);

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

    private User findUserByMail(String userMail) {
        final User user;

        if (usersCache.containsKey(userMail)) {
            user = usersCache.get(userMail);
        } else {
            user = userRepository.findByEmail(userMail)
                    .orElseThrow(NotFoundException::new);
            usersCache.put(userMail, user);
        }

        return user;
    }

    public void clearUsersCache() {
        usersCache.clear();
    }

    public void updateCachedUser(@NonNull User user) {
        if (usersCache.containsKey(user.getEmail()))
            usersCache.replace(user.getEmail(), user);
    }
}
