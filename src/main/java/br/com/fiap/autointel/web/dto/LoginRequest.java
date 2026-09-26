package br.com.fiap.autointel.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "analista@autointel.com") @NotBlank @Email String email,
        @Schema(example = "Analista@123") @NotBlank String senha) {
}
