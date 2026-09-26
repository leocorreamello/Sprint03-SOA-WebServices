package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.config.OpenApiConfig;
import br.com.fiap.autointel.service.VeiculoService;
import br.com.fiap.autointel.web.dto.EspecificacaoRequest;
import br.com.fiap.autointel.web.dto.EspecificacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Sub-recurso: especificações técnicas de um veículo, identificadas pelo código do atributo. */
@RestController
@RequestMapping("/api/v1/veiculos/{veiculoId}/especificacoes")
@Tag(name = "Especificações")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class EspecificacaoController {

    private final VeiculoService veiculoService;

    public EspecificacaoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as especificações cadastradas do veículo (ADMIN, ANALISTA)")
    @ApiResponse(responseCode = "200", description = "Especificações do veículo")
    @ApiResponse(responseCode = "404", description = "Veículo inexistente")
    public List<EspecificacaoResponse> listar(@PathVariable Long veiculoId) {
        return veiculoService.listarEspecificacoes(veiculoId).stream().map(EspecificacaoResponse::de).toList();
    }

    @GetMapping("/{codigoAtributo}")
    @Operation(summary = "Obtém o valor de um atributo para o veículo (ADMIN, ANALISTA)")
    @ApiResponse(responseCode = "200", description = "Especificação encontrada")
    @ApiResponse(responseCode = "404", description = "Veículo, atributo ou valor inexistente")
    public EspecificacaoResponse buscar(@PathVariable Long veiculoId, @PathVariable String codigoAtributo) {
        return EspecificacaoResponse.de(veiculoService.buscarEspecificacao(veiculoId, codigoAtributo));
    }

    @PutMapping("/{codigoAtributo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria ou substitui o valor de um atributo para o veículo (ADMIN, idempotente)")
    @ApiResponse(responseCode = "201", description = "Especificação criada")
    @ApiResponse(responseCode = "200", description = "Especificação substituída")
    @ApiResponse(responseCode = "404", description = "Veículo ou atributo inexistente")
    public ResponseEntity<EspecificacaoResponse> definir(@PathVariable Long veiculoId,
                                                         @PathVariable String codigoAtributo,
                                                         @Valid @RequestBody EspecificacaoRequest request) {
        var resultado = veiculoService.definirEspecificacao(veiculoId, codigoAtributo, request);
        var corpo = EspecificacaoResponse.de(resultado.especificacao());
        if (resultado.criada()) {
            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().build().toUri()).body(corpo);
        }
        return ResponseEntity.status(HttpStatus.OK).body(corpo);
    }

    @DeleteMapping("/{codigoAtributo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove o valor de um atributo do veículo (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Especificação removida")
    @ApiResponse(responseCode = "404", description = "Veículo, atributo ou valor inexistente")
    public ResponseEntity<Void> remover(@PathVariable Long veiculoId, @PathVariable String codigoAtributo) {
        veiculoService.removerEspecificacao(veiculoId, codigoAtributo);
        return ResponseEntity.noContent().build();
    }
}
