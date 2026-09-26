package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.config.OpenApiConfig;
import br.com.fiap.autointel.domain.model.Atributo;
import br.com.fiap.autointel.domain.model.CategoriaAtributo;
import br.com.fiap.autointel.service.AtributoService;
import br.com.fiap.autointel.web.dto.AtributoRequest;
import br.com.fiap.autointel.web.dto.AtributoResponse;
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
@RequestMapping("/api/v1/atributos")
@Tag(name = "Atributos")
public class AtributoController {

    private final AtributoService atributoService;

    public AtributoController(AtributoService atributoService) {
        this.atributoService = atributoService;
    }

    @GetMapping
    @Operation(summary = "Lista o catálogo de atributos reconhecidos (público)")
    @ApiResponse(responseCode = "200", description = "Catálogo de atributos")
    public List<AtributoResponse> listar(@RequestParam(required = false) CategoriaAtributo categoria) {
        return atributoService.listar(categoria).stream().map(AtributoResponse::de).toList();
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Detalha um atributo e seus sinônimos (público)")
    @ApiResponse(responseCode = "200", description = "Atributo encontrado")
    @ApiResponse(responseCode = "404", description = "Atributo inexistente")
    public AtributoResponse buscar(@PathVariable String codigo) {
        return AtributoResponse.de(atributoService.buscar(codigo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um novo atributo no catálogo (ADMIN)", security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "201", description = "Atributo criado")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "409", description = "Código já existente")
    public ResponseEntity<AtributoResponse> criar(@Valid @RequestBody AtributoRequest request) {
        Atributo atributo = atributoService.criar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{codigo}")
                .buildAndExpand(atributo.getCodigo()).toUri();
        return ResponseEntity.created(location).body(AtributoResponse.de(atributo));
    }

    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza nome, categoria, unidade e sinônimos de um atributo (ADMIN)",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "200", description = "Atributo atualizado")
    @ApiResponse(responseCode = "404", description = "Atributo inexistente")
    public AtributoResponse atualizar(@PathVariable String codigo, @Valid @RequestBody AtributoRequest request) {
        return AtributoResponse.de(atributoService.atualizar(codigo, request));
    }

    @DeleteMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um atributo sem especificações vinculadas (ADMIN)",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "204", description = "Atributo removido")
    @ApiResponse(responseCode = "404", description = "Atributo inexistente")
    @ApiResponse(responseCode = "409", description = "Atributo em uso")
    public ResponseEntity<Void> remover(@PathVariable String codigo) {
        atributoService.remover(codigo);
        return ResponseEntity.noContent().build();
    }
}
