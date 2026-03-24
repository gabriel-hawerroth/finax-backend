package br.finax.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * Handler CSRF específico para SPAs (Angular, React, etc.) com CookieCsrfTokenRepository.
 *
 * Resolve o conflito entre:
 * - BREACH protection (requer XOR na resposta)
 * - Angular enviando valor bruto do cookie no header X-XSRF-TOKEN (requer sem XOR na leitura via header)
 *
 * Regra de resolução:
 * - Token via header X-XSRF-TOKEN → usa CsrfTokenRequestAttributeHandler (sem XOR, valor bruto)
 * - Token via parâmetro _csrf     → usa XorCsrfTokenRequestAttributeHandler (com XOR, formulários SSR)
 *
 * Fluxo:
 * 1. POST /api/login com header X-XSRF-TOKEN (valor bruto do cookie) → resolveCsrfTokenValue() detecta header
 *    → retorna super.resolveCsrfTokenValue() (sem XOR, pois angular envia valor direto)
 * 2. POST /api/form com parâmetro _csrf (XOR/Base64) → resolveCsrfTokenValue() não encontra header
 *    → retorna this.delegate.resolveCsrfTokenValue() (com XOR, para formulários)
 * 3. handle() sempre usa XorCsrfTokenRequestAttributeHandler para BREACH protection na geração do token
 *
 * Referência: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#servlet-csrf-cookie
 */
public class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       Supplier<CsrfToken> csrfToken) {
        // IMPORTANTE: chamar AMBOS os métodos para garantir:
        // 1. super.handle() popula o atributo "_csrf" no request para que CsrfCookieFilter possa acessá-lo
        // 2. this.delegate.handle() aplica XOR e escreve o cookie com BREACH protection
        super.handle(request, response, csrfToken);
        this.delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        if (StringUtils.hasText(headerValue)) {
            // Token veio via header X-XSRF-TOKEN (Angular leu do cookie e enviou bruto)
            // Usa CsrfTokenRequestAttributeHandler (super) → sem XOR, valor bruto
            return super.resolveCsrfTokenValue(request, csrfToken);
        }
        // Token veio via parâmetro _csrf (formulário SSR ou POST form-encoded)
        // Usa XorCsrfTokenRequestAttributeHandler → com XOR, valor codificado
        return this.delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}
