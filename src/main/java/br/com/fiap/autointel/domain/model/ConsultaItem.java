package br.com.fiap.autointel.domain.model;

import jakarta.persistence.*;

/**
 * Uma linha da lista padronizada de especificações. Todos os campos existem em todas as
 * linhas; quando a informação não existe, {@code valor} fica nulo e {@code status} explica o motivo.
 */
@Entity
@Table(name = "consulta_itens")
public class ConsultaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    private Consulta consulta;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 120)
    private String termoSolicitado;

    @Column(length = 60)
    private String codigoAtributo;

    @Column(length = 120)
    private String nomeAtributo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CategoriaAtributo categoria;

    @Column(length = 500)
    private String valor;

    @Column(length = 20)
    private String unidade;

    @Column(length = 255)
    private String fonte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEspecificacao status;

    protected ConsultaItem() {
    }

    public ConsultaItem(String termoSolicitado, String codigoAtributo, String nomeAtributo,
                        CategoriaAtributo categoria, String valor, String unidade, String fonte,
                        StatusEspecificacao status) {
        this.termoSolicitado = termoSolicitado;
        this.codigoAtributo = codigoAtributo;
        this.nomeAtributo = nomeAtributo;
        this.categoria = categoria;
        this.valor = valor;
        this.unidade = unidade;
        this.fonte = fonte;
        this.status = status;
    }

    void vincular(Consulta consulta, int ordem) {
        this.consulta = consulta;
        this.ordem = ordem;
    }

    public Long getId() {
        return id;
    }

    public int getOrdem() {
        return ordem;
    }

    public String getTermoSolicitado() {
        return termoSolicitado;
    }

    public String getCodigoAtributo() {
        return codigoAtributo;
    }

    public String getNomeAtributo() {
        return nomeAtributo;
    }

    public CategoriaAtributo getCategoria() {
        return categoria;
    }

    public String getValor() {
        return valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getFonte() {
        return fonte;
    }

    public StatusEspecificacao getStatus() {
        return status;
    }
}
