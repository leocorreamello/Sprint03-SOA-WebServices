package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.config.OpenApiConfig;
import br.com.fiap.autointel.domain.model.Usuario;
import br.com.fiap.autointel.service.UsuarioService;
import br.com.fiap.autointel.web.dto.PerfilRequest;
import br.com.fiap.autointel.web.dto.RegistroRequest;
import br.com.fiap.autointel.web.dto.UsuarioResponse;
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
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Autenticação")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Cadastro público de usuário (sempre com perfil ANALISTA)")
    @ApiResponse(responseCode = "201", description = "Usuário criado (header Location aponta para o recurso)")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(location).body(UsuarioResponse.de(usuario));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista usuários (ADMIN)", security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "200", description = "Lista de usuários")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalha um usuário (ADMIN)", security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "404", description = "Usuário inexistente")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.de(usuarioService.buscar(id));
    }

    @PatchMapping("/{id}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Altera o perfil de acesso de um usuário (ADMIN)",
            description = "O novo perfil passa a valer no próximo token emitido para o usuário.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "200", description = "Perfil alterado")
    @ApiResponse(responseCode = "404", description = "Usuário inexistente")
    public UsuarioResponse alterarPerfil(@PathVariable Long id, @Valid @RequestBody PerfilRequest request) {
        return UsuarioResponse.de(usuarioService.alterarPerfil(id, request.perfil()));
    }
}
