package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PerfilRequest(@Schema(example = "ADMIN") @NotNull Perfil perfil) {
}
