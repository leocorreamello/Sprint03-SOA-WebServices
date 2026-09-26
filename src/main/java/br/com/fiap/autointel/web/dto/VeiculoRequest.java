package br.com.fiap.autointel.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VeiculoRequest(
        @Schema(example = "Toyota") @NotBlank @Size(max = 80) String marca,
        @Schema(example = "Hilux") @NotBlank @Size(max = 80) String modelo,
        @Schema(example = "SRX Plus") @NotBlank @Size(max = 120) String versao,
        @Schema(example = "2025") @Min(1950) @Max(2100) Integer anoModelo) {
}
