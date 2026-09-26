package br.com.fiap.autointel.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Responde 401 delegando ao {@code GlobalExceptionHandler}, garantindo o mesmo formato de erro da API.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public RestAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
        Object erroJwt = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERRO_JWT);
        AuthenticationException causa = erroJwt != null
                ? new TokenInvalidoException(erroJwt.toString())
                : new TokenInvalidoException(
                "Autenticação necessária. Envie um JWT válido no header 'Authorization: Bearer <token>'.");
        resolver.resolveException(request, response, null, causa);
    }
}
