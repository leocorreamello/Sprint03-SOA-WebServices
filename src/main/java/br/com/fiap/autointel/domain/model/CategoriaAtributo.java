package br.com.fiap.autointel.domain.model;

/**
 * Agrupamento padronizado dos atributos técnicos. A ordem de declaração é a ordem
 * de apresentação usada quando a saída é ordenada por categoria.
 */
public enum CategoriaAtributo {
    PRECO("Preço"),
    MOTOR("Motor"),
    TRANSMISSAO("Transmissão e tração"),
    DESEMPENHO("Desempenho"),
    SUSPENSAO("Suspensão e chassi"),
    RODAS_PNEUS("Rodas e pneus"),
    DIMENSOES("Dimensões"),
    CAPACIDADES("Capacidades"),
    OFF_ROAD("Off-road"),
    SEGURANCA("Segurança"),
    CONFORTO("Conforto"),
    TECNOLOGIA("Tecnologia e conectividade"),
    EXTERIOR("Exterior"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaAtributo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
