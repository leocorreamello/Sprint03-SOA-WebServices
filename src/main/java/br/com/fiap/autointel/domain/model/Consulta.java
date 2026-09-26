package br.com.fiap.autointel.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de uma consulta de especificações. Os itens guardam uma "fotografia" do
 * resultado no momento da consulta, garantindo que o histórico seja reproduzível
 * mesmo que a base de especificações seja alterada depois.
 */
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(nullable = false, length = 120)
    private String versao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Column(nullable = false, length = 160)
    private String solicitante;

    @Column(nullable = false)
    private Instant realizadaEm;

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ConsultaItem> itens = new ArrayList<>();

    protected Consulta() {
    }

    public Consulta(String marca, String modelo, String versao, Veiculo veiculo, String solicitante) {
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.versao = versao.trim();
        this.veiculo = veiculo;
        this.solicitante = solicitante;
        this.realizadaEm = Instant.now();
    }

    public void adicionarItem(ConsultaItem item) {
        item.vincular(this, itens.size() + 1);
        itens.add(item);
    }

    public long contar(StatusEspecificacao status) {
        return itens.stream().filter(i -> i.getStatus() == status).count();
    }

    public Long getId() {
        return id;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getVersao() {
        return versao;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public Instant getRealizadaEm() {
        return realizadaEm;
    }

    public List<ConsultaItem> getItens() {
        return itens;
    }
}
