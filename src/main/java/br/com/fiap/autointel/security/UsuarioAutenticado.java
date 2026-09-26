package br.com.fiap.autointel.security;

import br.com.fiap.autointel.domain.model.Perfil;

import java.time.Instant;

/**
 * Principal da requisição autenticada, montado exclusivamente a partir das claims do JWT
 * (sem consulta ao banco a cada requisição — autenticação stateless).
 */
public record UsuarioAutenticado(Long id, String email, String nome, Perfil perfil, Instant emitidoEm,
                                 Instant expiraEm) {

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }
}
