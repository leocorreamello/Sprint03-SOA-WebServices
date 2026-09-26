package br.com.fiap.autointel.domain.model;

/**
 * Situação de cada linha da lista padronizada de especificações.
 */
public enum StatusEspecificacao {
    /** O atributo foi reconhecido e o veículo possui valor cadastrado. */
    DISPONIVEL,
    /** O atributo foi reconhecido, mas não há informação para o veículo (ou o veículo não foi encontrado). */
    NAO_DISPONIVEL,
    /** O termo informado pelo usuário não corresponde a nenhum atributo do catálogo. */
    ATRIBUTO_NAO_RECONHECIDO
}
