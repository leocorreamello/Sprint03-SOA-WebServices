package br.com.fiap.autointel.unit;

import br.com.fiap.autointel.domain.model.Perfil;
import br.com.fiap.autointel.domain.model.Usuario;
import br.com.fiap.autointel.security.JwtProperties;
import br.com.fiap.autointel.security.JwtService;
import br.com.fiap.autointel.security.UsuarioAutenticado;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService (geração e validação de JWT)")
class JwtServiceTest {

    private static final String SEGREDO = Base64.getEncoder()
            .encodeToString("chave-de-teste-com-mais-de-256-bits-para-hs256!!".getBytes());
    private static final Instant AGORA = Instant.parse("2026-01-01T12:00:00Z");

    private final JwtProperties props = new JwtProperties(SEGREDO, Duration.ofHours(1), "autointel-api");

    private static Usuario usuario() {
        Usuario u = new Usuario("Ana", "ana@teste.com", "hash", Perfil.ANALISTA);
        ReflectionTestUtils.setField(u, "id", 42L);
        return u;
    }

    private JwtService servicoEm(Instant instante) {
        return new JwtService(props, Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void deveGerarTokenComClaimsEExpiracaoConfigurada() {
        JwtService.TokenGerado gerado = servicoEm(AGORA).gerar(usuario());

        assertThat(gerado.token().split("\\.")).hasSize(3);
        assertThat(gerado.emitidoEm()).isEqualTo(AGORA);
        assertThat(gerado.expiraEm()).isEqualTo(AGORA.plus(Duration.ofHours(1)));

        UsuarioAutenticado u = servicoEm(AGORA.plusSeconds(60)).validar(gerado.token());
        assertThat(u.id()).isEqualTo(42L);
        assertThat(u.email()).isEqualTo("ana@teste.com");
        assertThat(u.nome()).isEqualTo("Ana");
        assertThat(u.perfil()).isEqualTo(Perfil.ANALISTA);
        assertThat(u.expiraEm()).isEqualTo(gerado.expiraEm());
    }

    @Test
    void deveRejeitarTokenExpirado() {
        String token = servicoEm(AGORA).gerar(usuario()).token();
        JwtService depoisDaExpiracao = servicoEm(AGORA.plus(Duration.ofHours(1)).plusSeconds(1));

        assertThatThrownBy(() -> depoisDaExpiracao.validar(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = servicoEm(AGORA).gerar(usuario()).token();
        String[] partes = token.split("\\.");
        String payloadAdulterado = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"ana@teste.com\",\"uid\":42,\"perfil\":\"ADMIN\",\"iss\":\"autointel-api\"}".getBytes());
        String adulterado = partes[0] + "." + payloadAdulterado + "." + partes[2];

        assertThatThrownBy(() -> servicoEm(AGORA).validar(adulterado)).isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        String outroSegredo = Base64.getEncoder().encodeToString("outra-chave-secreta-com-mais-de-256-bits-xxxxx".getBytes());
        JwtService outro = new JwtService(new JwtProperties(outroSegredo, Duration.ofHours(1), "autointel-api"),
                Clock.fixed(AGORA, ZoneOffset.UTC));
        String token = outro.gerar(usuario()).token();

        assertThatThrownBy(() -> servicoEm(AGORA).validar(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTokenDeOutroEmissor() {
        JwtService outroEmissor = new JwtService(new JwtProperties(SEGREDO, Duration.ofHours(1), "outro-sistema"),
                Clock.fixed(AGORA, ZoneOffset.UTC));
        String token = outroEmissor.gerar(usuario()).token();

        assertThatThrownBy(() -> servicoEm(AGORA).validar(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTextoQueNaoEJwt() {
        assertThatThrownBy(() -> servicoEm(AGORA).validar("nao-sou-um-jwt")).isInstanceOf(JwtException.class);
    }
}
