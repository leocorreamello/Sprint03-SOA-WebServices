package br.com.fiap.autointel.domain.model;

import br.com.fiap.autointel.domain.NormalizadorTexto;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(nullable = false, length = 120)
    private String versao;

    private Integer anoModelo;

    /** Chave normalizada "marca|modelo|versao" usada na busca tolerante a acentos/caixa. */
    @Column(nullable = false, unique = true, length = 300)
    private String chaveBusca;

    @OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Especificacao> especificacoes = new ArrayList<>();

    protected Veiculo() {
    }

    public Veiculo(String marca, String modelo, String versao, Integer anoModelo) {
        atualizar(marca, modelo, versao, anoModelo);
    }

    public static String gerarChave(String marca, String modelo, String versao) {
        return NormalizadorTexto.normalizar(marca) + "|" + NormalizadorTexto.normalizar(modelo) + "|"
                + NormalizadorTexto.normalizar(versao);
    }

    public void atualizar(String marca, String modelo, String versao, Integer anoModelo) {
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.versao = versao.trim();
        this.anoModelo = anoModelo;
        this.chaveBusca = gerarChave(marca, modelo, versao);
    }

    public Optional<Especificacao> especificacaoDe(String codigoAtributo) {
        return especificacoes.stream()
                .filter(e -> e.getAtributo().getCodigo().equals(codigoAtributo))
                .findFirst();
    }

    /** Cria ou atualiza o valor do atributo. Retorna {@code true} quando a especificação é nova. */
    public boolean definirEspecificacao(Atributo atributo, String valor, String fonte) {
        Optional<Especificacao> existente = especificacaoDe(atributo.getCodigo());
        if (existente.isPresent()) {
            existente.get().atualizar(valor, fonte);
            return false;
        }
        especificacoes.add(new Especificacao(this, atributo, valor, fonte));
        return true;
    }

    public boolean removerEspecificacao(String codigoAtributo) {
        return especificacoes.removeIf(e -> e.getAtributo().getCodigo().equals(codigoAtributo));
    }

    public String descricao() {
        return marca + " " + modelo + " " + versao;
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

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public String getChaveBusca() {
        return chaveBusca;
    }

    public List<Especificacao> getEspecificacoes() {
        return especificacoes;
    }
}
