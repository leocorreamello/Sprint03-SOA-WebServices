package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.config.OpenApiConfig;
import br.com.fiap.autointel.security.UsuarioAutenticado;
import br.com.fiap.autointel.service.AuthService;
import br.com.fiap.autointel.web.dto.LoginRequest;
import br.com.fiap.autointel.web.dto.TokenResponse;
import br.com.fiap.autointel.web.dto.UsuarioAutenticadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail e senha e devolve um JWT (público)")
    @ApiResponse(responseCode = "200", description = "Token emitido")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário extraídos do token JWT", security = @SecurityRequirement(name = OpenApiConfig.BEARER))
    @ApiResponse(responseCode = "200", description = "Usuário autenticado")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    public UsuarioAutenticadoResponse me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return UsuarioAutenticadoResponse.de(usuario);
    }
}
