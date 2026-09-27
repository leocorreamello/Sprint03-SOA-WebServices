package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.model.Usuario;
import br.com.fiap.autointel.domain.repository.UsuarioRepository;
import br.com.fiap.autointel.security.JwtService;
import br.com.fiap.autointel.security.LoginThrottle;
import br.com.fiap.autointel.web.dto.LoginRequest;
import br.com.fiap.autointel.web.dto.TokenResponse;
import br.com.fiap.autointel.web.dto.UsuarioResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final JwtService jwtService;
    private final LoginThrottle loginThrottle;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
                       JwtService jwtService, LoginThrottle loginThrottle) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.jwtService = jwtService;
        this.loginThrottle = loginThrottle;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request, String remoteAddress) {
        loginThrottle.check(remoteAddress, request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        } catch (BadCredentialsException ex) {
            loginThrottle.failed(remoteAddress, request.email());
            log.warn("security_event=LOGIN_FAILURE");
            throw ex;
        }
        loginThrottle.succeeded(remoteAddress, request.email());
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        log.info("security_event=LOGIN_SUCCESS user_id={}", usuario.getId());
        return emitirToken(usuario);
    }

    private TokenResponse emitirToken(Usuario usuario) {
        JwtService.TokenGerado gerado = jwtService.gerar(usuario);
        return new TokenResponse("Bearer", gerado.token(), jwtService.expiracaoEmSegundos(), gerado.expiraEm(),
                UsuarioResponse.de(usuario));
    }
}
