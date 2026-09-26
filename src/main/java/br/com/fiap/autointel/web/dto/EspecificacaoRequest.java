package br.com.fiap.autointel.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecificacaoRequest(
        @Schema(example = "397", description = "Valor sem a unidade (a unidade vem do catálogo de atributos)")
        @NotBlank @Size(max = 500) String valor,
        @Schema(example = "Ficha técnica oficial Ford Brasil") @Size(max = 255) String fonte) {
}
