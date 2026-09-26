package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.config.OpenApiConfig;
import br.com.fiap.autointel.domain.model.Veiculo;
import br.com.fiap.autointel.service.VeiculoService;
import br.com.fiap.autointel.web.dto.VeiculoRequest;
import br.com.fiap.autointel.web.dto.VeiculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Veículos")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    @Operation(summary = "Lista veículos cadastrados, com filtro opcional por marca e modelo (ADMIN, ANALISTA)")
    @ApiResponse(responseCode = "200", description = "Lista de veículos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    public List<VeiculoResponse> listar(@RequestParam(required = false) String marca,
                                        @RequestParam(required = false) String modelo) {
        return veiculoService.listar(marca, modelo).stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um veículo (ADMIN, ANALISTA)")
    @ApiResponse(responseCode = "200", description = "Veículo encontrado")
    @ApiResponse(responseCode = "404", description = "Veículo inexistente")
    public VeiculoResponse buscar(@PathVariable Long id) {
        return VeiculoResponse.de(veiculoService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um veículo da concorrência (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Veículo criado")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "409", description = "Veículo (marca/modelo/versão) já existe")
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoRequest request) {
        Veiculo veiculo = veiculoService.criar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(veiculo.getId()).toUri();
        return ResponseEntity.created(location).body(VeiculoResponse.de(veiculo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza os dados de identificação do veículo (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Veículo atualizado")
    @ApiResponse(responseCode = "404", description = "Veículo inexistente")
    @ApiResponse(responseCode = "409", description = "Conflito com outro veículo")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return VeiculoResponse.de(veiculoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove o veículo e suas especificações (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Veículo removido")
    @ApiResponse(responseCode = "404", description = "Veículo inexistente")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        veiculoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
