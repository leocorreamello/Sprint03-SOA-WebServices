package br.com.fiap.autointel.exception;

import org.springframework.security.access.AccessDeniedException;

/** Usuário autenticado tentando acessar um recurso que não lhe pertence → HTTP 403. */
public class AcessoNegadoException extends AccessDeniedException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
