package br.com.fiap.autointel.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConsultaRequest(
        @Schema(example = "Ford") @NotBlank @Size(max = 80) String marca,
        @Schema(example = "Ranger") @NotBlank @Size(max = 80) String modelo,
        @Schema(example = "Raptor") @NotBlank @Size(max = 120) String versao,
        @Schema(description = "Lista livre de equipamentos/atributos técnicos a pesquisar. A saída respeita esta ordem.",
                example = "[\"Preço\", \"Motor\", \"Potência\", \"Torque\", \"Câmbio\", \"Tração\", \"Teto solar\"]")
        @NotEmpty @Size(max = 150) List<@NotBlank @Size(max = 120) String> atributos) {
}
