package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.model.Atributo;
import br.com.fiap.autointel.domain.model.Especificacao;
import br.com.fiap.autointel.domain.model.Veiculo;
import br.com.fiap.autointel.domain.repository.ConsultaRepository;
import br.com.fiap.autointel.domain.repository.VeiculoRepository;
import br.com.fiap.autointel.exception.ConflitoException;
import br.com.fiap.autointel.exception.RecursoNaoEncontradoException;
import br.com.fiap.autointel.web.dto.EspecificacaoRequest;
import br.com.fiap.autointel.web.dto.VeiculoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculos;
    private final ConsultaRepository consultas;
    private final AtributoService atributoService;

    public VeiculoService(VeiculoRepository veiculos, ConsultaRepository consultas, AtributoService atributoService) {
        this.veiculos = veiculos;
        this.consultas = consultas;
        this.atributoService = atributoService;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listar(String marca, String modelo) {
        List<Veiculo> lista = veiculos.buscar(vazioParaNulo(marca), vazioParaNulo(modelo));
        lista.forEach(v -> v.getEspecificacoes().size()); // inicializa a coleção para o DTO
        return lista;
    }

    @Transactional(readOnly = true)
    public Veiculo buscar(Long id) {
        Veiculo veiculo = veiculos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo " + id + " não encontrado"));
        veiculo.getEspecificacoes().size();
        return veiculo;
    }

    @Transactional
    public Veiculo criar(VeiculoRequest request) {
        garantirUnico(request, null);
        return veiculos.save(new Veiculo(request.marca(), request.modelo(), request.versao(), request.anoModelo()));
    }

    @Transactional
    public Veiculo atualizar(Long id, VeiculoRequest request) {
        Veiculo veiculo = buscar(id);
        garantirUnico(request, id);
        veiculo.atualizar(request.marca(), request.modelo(), request.versao(), request.anoModelo());
        return veiculo;
    }

    @Transactional
    public void remover(Long id) {
        Veiculo veiculo = buscar(id);
        consultas.desvincularVeiculo(veiculo);
        veiculos.delete(veiculo);
    }

    @Transactional(readOnly = true)
    public List<Especificacao> listarEspecificacoes(Long id) {
        return buscar(id).getEspecificacoes().stream()
                .sorted(Comparator.comparing((Especificacao e) -> e.getAtributo().getCategoria())
                        .thenComparing(e -> e.getAtributo().getNome()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Especificacao buscarEspecificacao(Long id, String codigoAtributo) {
        Veiculo veiculo = buscar(id);
        Atributo atributo = atributoService.buscar(codigoAtributo);
        return veiculo.especificacaoDe(atributo.getCodigo())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "O veículo " + id + " não possui valor para o atributo " + atributo.getCodigo()));
    }

    public record ResultadoDefinicao(Especificacao especificacao, boolean criada) {
    }

    /** PUT idempotente: cria (201) ou substitui (200) o valor do atributo para o veículo. */
    @Transactional
    public ResultadoDefinicao definirEspecificacao(Long id, String codigoAtributo, EspecificacaoRequest request) {
        Veiculo veiculo = buscar(id);
        Atributo atributo = atributoService.buscar(codigoAtributo);
        boolean criada = veiculo.definirEspecificacao(atributo, request.valor(), request.fonte());
        veiculos.flush();
        return new ResultadoDefinicao(veiculo.especificacaoDe(atributo.getCodigo()).orElseThrow(), criada);
    }

    @Transactional
    public void removerEspecificacao(Long id, String codigoAtributo) {
        Veiculo veiculo = buscar(id);
        Atributo atributo = atributoService.buscar(codigoAtributo);
        if (!veiculo.removerEspecificacao(atributo.getCodigo())) {
            throw new RecursoNaoEncontradoException(
                    "O veículo " + id + " não possui valor para o atributo " + atributo.getCodigo());
        }
    }

    private void garantirUnico(VeiculoRequest request, Long idAtual) {
        veiculos.findByChaveBusca(Veiculo.gerarChave(request.marca(), request.modelo(), request.versao()))
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ConflitoException("Já existe o veículo " + existente.descricao()
                            + " (id " + existente.getId() + ")");
                });
    }

    private static String vazioParaNulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
