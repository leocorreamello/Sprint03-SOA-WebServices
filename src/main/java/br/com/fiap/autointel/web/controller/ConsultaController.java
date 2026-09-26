package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.config.OpenApiConfig;
import br.com.fiap.autointel.domain.model.Consulta;
import br.com.fiap.autointel.security.UsuarioAutenticado;
import br.com.fiap.autointel.service.ConsultaService;
import br.com.fiap.autointel.web.dto.ConsultaRequest;
import br.com.fiap.autointel.web.dto.ConsultaResponse;
import br.com.fiap.autointel.web.dto.ConsultaResumoResponse;
import br.com.fiap.autointel.web.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/consultas")
@Tag(name = "Consultas")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class ConsultaController {

    public static final String TEXT_CSV = "text/csv";

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    @Operation(summary = "Realiza uma consulta de especificações e devolve a lista padronizada",
            description = """
                    Informe marca, modelo, versão e a lista **livre** de atributos. Cada termo gera exatamente uma \
                    linha na saída, na mesma ordem enviada, com os campos fixos: ordem, termoSolicitado, codigo, \
                    atributo, categoria, valor, unidade, valorFormatado, fonte e status \
                    (DISPONIVEL | NAO_DISPONIVEL | ATRIBUTO_NAO_RECONHECIDO).""")
    @ApiResponse(responseCode = "201", description = "Consulta registrada; header Location aponta para ela")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    public ResponseEntity<ConsultaResponse> realizar(@Valid @RequestBody ConsultaRequest request,
                                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        Consulta consulta = consultaService.realizar(request, usuario);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(consulta.getId()).toUri();
        return ResponseEntity.created(location).body(ConsultaResponse.de(consulta));
    }

    @GetMapping
    @Operation(summary = "Histórico paginado de consultas (ANALISTA vê as próprias; ADMIN vê todas)")
    @ApiResponse(responseCode = "200", description = "Página de consultas")
    public PaginaResponse<ConsultaResumoResponse> listar(
            @ParameterObject @PageableDefault(size = 20, sort = "realizadaEm", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PaginaResponse.de(consultaService.listar(usuario, pageable), ConsultaResumoResponse::de);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtém o resultado de uma consulta (JSON ou CSV via header Accept: text/csv)")
    @ApiResponse(responseCode = "200", description = "Consulta encontrada", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ConsultaResponse.class)),
            @Content(mediaType = TEXT_CSV, schema = @Schema(type = "string"))})
    @ApiResponse(responseCode = "403", description = "Consulta pertence a outro usuário")
    @ApiResponse(responseCode = "404", description = "Consulta inexistente")
    public ConsultaResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ConsultaResponse.de(consultaService.buscar(id, usuario));
    }

    @GetMapping(value = "/{id}", produces = TEXT_CSV)
    @Operation(hidden = true)
    public ResponseEntity<String> exportarCsv(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ConsultaResponse consulta = ConsultaResponse.de(consultaService.buscar(id, usuario));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(TEXT_CSV + ";charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consulta-" + id + ".csv\"")
                .body(CsvConsultaWriter.escrever(consulta));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma consulta do histórico (dono da consulta ou ADMIN)")
    @ApiResponse(responseCode = "204", description = "Consulta removida")
    @ApiResponse(responseCode = "403", description = "Consulta pertence a outro usuário")
    @ApiResponse(responseCode = "404", description = "Consulta inexistente")
    public ResponseEntity<Void> remover(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        consultaService.remover(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
