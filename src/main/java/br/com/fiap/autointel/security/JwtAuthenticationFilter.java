package br.com.fiap.autointel.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lê o header {@code Authorization: Bearer <jwt>}, valida o token e popula o SecurityContext.
 * Em caso de falha o motivo é guardado na requisição para que o {@link RestAuthenticationEntryPoint}
 * devolva um 401 explicativo (ex.: "Token expirado").
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERRO_JWT = "autointel.jwt.erro";
    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, PREFIXO, 0, PREFIXO.length())) {
            String token = header.substring(PREFIXO.length()).trim();
            try {
                UsuarioAutenticado usuario = jwtService.validar(token);
                var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null,
                        List.of(new SimpleGrantedAuthority(usuario.perfil().authority())));
                autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } catch (ExpiredJwtException e) {
                request.setAttribute(ATRIBUTO_ERRO_JWT, "Token expirado. Faça login novamente para obter um novo token.");
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ATRIBUTO_ERRO_JWT, "Token inválido: assinatura, formato ou emissor não reconhecidos.");
            }
        }
        chain.doFilter(request, response);
    }
}
