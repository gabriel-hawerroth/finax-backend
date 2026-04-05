package br.finax.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que força a materialização do token CSRF diferido (deferred) em cada requisição.
 *
 * CHAVE CORRETA: "_csrf" (não CsrfToken.class.getName())
 *
 * Por que "_csrf" e não CsrfToken.class.getName():
 * - CsrfToken.class.getName() retorna o DeferredCsrfToken wrappado pelo SpaCsrfTokenRequestHandler.
 *   Chamar getToken() nele invoca o delegate XorCsrfTokenRequestAttributeHandler, que grava um
 *   valor XOR-mascarado/Base64 no cookie XSRF-TOKEN. Na próxima requisição, o Angular envia esse
 *   valor mascarado no header X-XSRF-TOKEN, mas o SpaCsrfTokenRequestHandler tenta validá-lo
 *   como UUID bruto (pois veio via header) — a comparação falha e retorna 403.
 *
 * - "_csrf" é o atributo populado pelo CsrfTokenRequestAttributeHandler (superclasse do
 *   SpaCsrfTokenRequestHandler) após processar o handle(). O CsrfToken acessado por essa chave
 *   expõe o UUID bruto ao chamar getToken(), garantindo que o cookie XSRF-TOKEN seja sempre
 *   reescrito com o valor bruto que o Angular espera.
 *
 * Posicionamento: deve ser registrado APÓS o filtro de autenticação customizado (SecurityFilter),
 * para que o token seja materializado depois que a identidade já foi estabelecida.
 *
 * Referência oficial: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // "_csrf" é a chave correta: retorna o CsrfToken processado pelo handler,
        // não o DeferredCsrfToken wrappado com XOR. Isso garante que getToken()
        // force a materialização do cookie com UUID bruto, não com valor mascarado.
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            // Força materialização: Spring reescreve Set-Cookie: XSRF-TOKEN=<uuid-bruto>
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
