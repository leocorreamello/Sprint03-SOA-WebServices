package br.com.fiap.autointel.security;

import br.com.fiap.autointel.domain.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Usado apenas no login (e-mail + senha) para verificar as credenciais com BCrypt. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return repositorio.findByEmailIgnoreCase(email)
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getSenha())
                        .authorities(u.getPerfil().authority())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
