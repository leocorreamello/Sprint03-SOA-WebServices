package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.CategoriaAtributo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AtributoRequest(
        @Schema(example = "CONSUMO_URBANO", description = "Código único em MAIÚSCULAS_COM_UNDERSCORE (ignorado no PUT)")
        @Pattern(regexp = "^[A-Z0-9_]{2,60}$", message = "deve conter apenas letras maiúsculas, números e _")
        String codigo,
        @Schema(example = "Consumo urbano (gasolina)") @NotBlank @Size(max = 120) String nome,
        @Schema(example = "DESEMPENHO") @NotNull CategoriaAtributo categoria,
        @Schema(example = "km/l") @Size(max = 20) String unidade,
        @Schema(example = "[\"consumo cidade\", \"km/l cidade\"]") Set<@NotBlank @Size(max = 120) String> sinonimos) {
}
