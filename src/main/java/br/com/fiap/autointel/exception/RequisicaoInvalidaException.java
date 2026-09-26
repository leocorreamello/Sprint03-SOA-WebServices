package br.com.fiap.autointel.exception;

/** Regra de validação que depende do contexto da operação → HTTP 400. */
public class RequisicaoInvalidaException extends RuntimeException {

    private final String campo;

    public RequisicaoInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
