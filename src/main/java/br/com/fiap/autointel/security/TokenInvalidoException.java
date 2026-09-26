package br.com.fiap.autointel.security;

import org.springframework.security.core.AuthenticationException;

/** Token ausente, malformado, com assinatura inválida ou expirado → HTTP 401. */
public class TokenInvalidoException extends AuthenticationException {

    public TokenInvalidoException(String mensagem) {
        super(mensagem);
    }
}
