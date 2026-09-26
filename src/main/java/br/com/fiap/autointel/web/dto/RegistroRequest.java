package br.com.fiap.autointel.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @Schema(example = "Maria Souza") @NotBlank @Size(max = 120) String nome,
        @Schema(example = "maria@empresa.com") @NotBlank @Email @Size(max = 160) String email,
        @Schema(example = "Senha@123", description = "Mínimo 8 caracteres, com letra e número")
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "deve conter ao menos uma letra e um número")
        String senha) {
}
