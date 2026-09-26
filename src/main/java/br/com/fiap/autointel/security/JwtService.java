package br.com.fiap.autointel.security;

import br.com.fiap.autointel.domain.model.Perfil;
import br.com.fiap.autointel.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Geração e validação de JWT assinados com HMAC-SHA256.
 * <p>Claims emitidas:</p>
 * <ul>
 *     <li>{@code sub} – e-mail do usuário</li>
 *     <li>{@code uid} – id do usuário</li>
 *     <li>{@code nome} – nome do usuário</li>
 *     <li>{@code perfil} – ADMIN | ANALISTA (usado na autorização)</li>
 *     <li>{@code iss}, {@code iat}, {@code exp}, {@code jti}</li>
 * </ul>
 */
@Service
public class JwtService {

    static final String CLAIM_ID = "uid";
    static final String CLAIM_NOME = "nome";
    static final String CLAIM_PERFIL = "perfil";

    private final SecretKey chave;
    private final JwtProperties propriedades;
    private final Clock relogio;

    @Autowired
    public JwtService(JwtProperties propriedades) {
        this(propriedades, Clock.systemUTC());
    }

    public JwtService(JwtProperties propriedades, Clock relogio) {
        this.propriedades = propriedades;
        this.relogio = relogio;
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(propriedades.segredo()));
    }

    public record TokenGerado(String token, Instant emitidoEm, Instant expiraEm) {
    }

    public TokenGerado gerar(Usuario usuario) {
        Instant agora = relogio.instant();
        Instant expiraEm = agora.plus(propriedades.expiracao());
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(propriedades.emissor())
                .subject(usuario.getEmail())
                .claim(CLAIM_ID, usuario.getId())
                .claim(CLAIM_NOME, usuario.getNome())
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
        return new TokenGerado(token, agora, expiraEm);
    }

    /**
     * Valida assinatura, emissor e expiração e devolve o usuário contido no token.
     *
     * @throws io.jsonwebtoken.ExpiredJwtException se o token estiver expirado
     * @throws JwtException                        se o token for inválido por qualquer outro motivo
     */
    public UsuarioAutenticado validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .requireIssuer(propriedades.emissor())
                .clock(() -> Date.from(relogio.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        try {
            return new UsuarioAutenticado(
                    claims.get(CLAIM_ID, Number.class).longValue(),
                    claims.getSubject(),
                    claims.get(CLAIM_NOME, String.class),
                    Perfil.valueOf(claims.get(CLAIM_PERFIL, String.class)),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant());
        } catch (RuntimeException e) {
            throw new JwtException("Token com claims obrigatórias ausentes ou inválidas", e);
        }
    }

    public long expiracaoEmSegundos() {
        return propriedades.expiracao().toSeconds();
    }
}
