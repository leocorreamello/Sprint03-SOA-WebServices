package br.com.fiap.autointel.domain.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Atributo técnico canônico do catálogo (ex.: POTENCIA, TORQUE, AIRBAGS).
 * Os sinônimos permitem que o usuário descreva livremente o que deseja pesquisar
 * ("potência", "cv", "horsepower"...) e ainda assim receba a saída padronizada.
 */
@Entity
@Table(name = "atributos")
public class Atributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaAtributo categoria;

    @Column(length = 20)
    private String unidade;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atributo_sinonimos", joinColumns = @JoinColumn(name = "atributo_id"))
    @Column(name = "sinonimo", nullable = false, length = 120)
    private Set<String> sinonimos = new LinkedHashSet<>();

    protected Atributo() {
    }

    public Atributo(String codigo, String nome, CategoriaAtributo categoria, String unidade, Set<String> sinonimos) {
        this.codigo = codigo;
        atualizar(nome, categoria, unidade, sinonimos);
    }

    public void atualizar(String nome, CategoriaAtributo categoria, String unidade, Set<String> sinonimos) {
        this.nome = nome;
        this.categoria = categoria;
        this.unidade = unidade == null || unidade.isBlank() ? null : unidade.trim();
        this.sinonimos.clear();
        if (sinonimos != null) {
            this.sinonimos.addAll(sinonimos);
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public CategoriaAtributo getCategoria() {
        return categoria;
    }

    public String getUnidade() {
        return unidade;
    }

    public Set<String> getSinonimos() {
        return sinonimos;
    }
}
