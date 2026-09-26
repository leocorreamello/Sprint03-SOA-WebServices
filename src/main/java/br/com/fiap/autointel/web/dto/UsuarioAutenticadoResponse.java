package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.Perfil;
import br.com.fiap.autointel.security.UsuarioAutenticado;

import java.time.Instant;

/** Informações extraídas diretamente das claims do JWT. */
public record UsuarioAutenticadoResponse(Long id, String nome, String email, Perfil perfil,
                                         Instant tokenEmitidoEm, Instant tokenExpiraEm) {

    public static UsuarioAutenticadoResponse de(UsuarioAutenticado u) {
        return new UsuarioAutenticadoResponse(u.id(), u.nome(), u.email(), u.perfil(), u.emitidoEm(), u.expiraEm());
    }
}
