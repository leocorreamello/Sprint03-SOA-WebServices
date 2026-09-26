package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.model.StatusEspecificacao;

import java.util.Set;

/**
 * Gera o texto legível de cada especificação, deixando explícita a ausência de informação.
 */
public final class FormatadorValor {

    public static final String NAO_DISPONIVEL = "Não disponível";
    public static final String NAO_RECONHECIDO = "Atributo não reconhecido";

    private static final Set<String> UNIDADES_PREFIXADAS = Set.of("R$", "US$", "€");
    private static final Set<String> UNIDADES_SEM_ESPACO = Set.of("°", "\"", "%");

    private FormatadorValor() {
    }

    public static String formatar(String valor, String unidade, StatusEspecificacao status) {
        return switch (status) {
            case NAO_DISPONIVEL -> NAO_DISPONIVEL;
            case ATRIBUTO_NAO_RECONHECIDO -> NAO_RECONHECIDO;
            case DISPONIVEL -> comUnidade(valor, unidade);
        };
    }

    public static String comUnidade(String valor, String unidade) {
        if (unidade == null || unidade.isBlank()) {
            return valor;
        }
        if (UNIDADES_PREFIXADAS.contains(unidade)) {
            return unidade + " " + valor;
        }
        if (UNIDADES_SEM_ESPACO.contains(unidade)) {
            return valor + unidade;
        }
        return valor + " " + unidade;
    }
}
