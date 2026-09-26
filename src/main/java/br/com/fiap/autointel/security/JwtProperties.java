package br.com.fiap.autointel.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;

/**
 * Configurações do JWT ({@code app.jwt.*} no application.properties).
 *
 * @param segredo   chave HMAC em Base64 (mínimo 256 bits para HS256)
 * @param expiracao tempo de vida do token (ex.: 1h, 30m)
 * @param emissor   valor da claim {@code iss}, validado na leitura do token
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(@NotBlank String segredo, @NotNull Duration expiracao, @NotBlank String emissor) {
}
