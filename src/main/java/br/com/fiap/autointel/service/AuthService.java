package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.model.Usuario;
import br.com.fiap.autointel.domain.repository.UsuarioRepository;
import br.com.fiap.autointel.security.JwtService;
import br.com.fiap.autointel.web.dto.LoginRequest;
import br.com.fiap.autointel.web.dto.TokenResponse;
import br.com.fiap.autointel.web.dto.UsuarioResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        return emitirToken(usuario);
    }

    private TokenResponse emitirToken(Usuario usuario) {
        JwtService.TokenGerado gerado = jwtService.gerar(usuario);
        return new TokenResponse("Bearer", gerado.token(), jwtService.expiracaoEmSegundos(), gerado.expiraEm(),
                UsuarioResponse.de(usuario));
    }
}
