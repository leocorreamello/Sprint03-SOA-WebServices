package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.model.Perfil;
import br.com.fiap.autointel.domain.model.Usuario;
import br.com.fiap.autointel.domain.repository.UsuarioRepository;
import br.com.fiap.autointel.exception.ConflitoException;
import br.com.fiap.autointel.exception.RecursoNaoEncontradoException;
import br.com.fiap.autointel.web.dto.RegistroRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    /** Auto-cadastro público: sempre cria usuários com perfil ANALISTA (promoção apenas por um ADMIN). */
    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário cadastrado com o e-mail " + email);
        }
        return usuarios.save(new Usuario(request.nome().trim(), email, passwordEncoder.encode(request.senha()),
                Perfil.ANALISTA));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.findAll(Sort.by("nome"));
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    @Transactional
    public Usuario alterarPerfil(Long id, Perfil perfil) {
        Usuario usuario = buscar(id);
        usuario.alterarPerfil(perfil);
        return usuario;
    }
}
