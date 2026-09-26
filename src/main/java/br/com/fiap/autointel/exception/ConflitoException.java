package br.com.fiap.autointel.exception;

/** Violação de unicidade / estado conflitante → HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
