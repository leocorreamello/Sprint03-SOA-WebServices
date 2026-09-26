package br.com.fiap.autointel.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Valor de um atributo técnico para um veículo específico.
 */
@Entity
@Table(name = "especificacoes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"veiculo_id", "atributo_id"}))
public class Especificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "atributo_id")
    private Atributo atributo;

    @Column(nullable = false, length = 500)
    private String valor;

    @Column(length = 255)
    private String fonte;

    @Column(nullable = false)
    private Instant atualizadoEm;

    protected Especificacao() {
    }

    Especificacao(Veiculo veiculo, Atributo atributo, String valor, String fonte) {
        this.veiculo = veiculo;
        this.atributo = atributo;
        atualizar(valor, fonte);
    }

    void atualizar(String valor, String fonte) {
        this.valor = valor.trim();
        this.fonte = fonte;
        this.atualizadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public Atributo getAtributo() {
        return atributo;
    }

    public String getValor() {
        return valor;
    }

    public String getFonte() {
        return fonte;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
