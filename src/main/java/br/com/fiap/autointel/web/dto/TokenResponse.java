package br.com.fiap.autointel.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record TokenResponse(
        @Schema(example = "Bearer") String tipo,
        @Schema(description = "JWT a ser enviado no header Authorization: Bearer <token>") String token,
        @Schema(description = "Validade do token em segundos", example = "3600") long expiraEmSegundos,
        Instant expiraEm,
        UsuarioResponse usuario) {
}
