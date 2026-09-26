package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.Perfil;
import br.com.fiap.autointel.domain.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
}
