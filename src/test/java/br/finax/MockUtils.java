package br.finax;

import br.finax.models.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UtilityClass
public class MockUtils {

    public static Authentication mockAuthentication() {
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        User mockUser = new User();
        mockUser.setId(1L);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        return authentication;
    }

    public static void mockAuthenticationUser(Authentication authentication, User user) {
        when(authentication.getPrincipal()).thenReturn(user);
    }

    public static void mockAuthenticationUserId(Authentication authentication, Long userId) {
        User user = (User) authentication.getPrincipal();
        user.setId(userId);
        when(authentication.getPrincipal()).thenReturn(user);
    }
}
