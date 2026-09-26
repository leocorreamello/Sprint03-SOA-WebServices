package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.NormalizadorTexto;
import br.com.fiap.autointel.domain.model.*;
import br.com.fiap.autointel.domain.repository.AtributoRepository;
import br.com.fiap.autointel.domain.repository.ConsultaRepository;
import br.com.fiap.autointel.domain.repository.VeiculoRepository;
import br.com.fiap.autointel.exception.AcessoNegadoException;
import br.com.fiap.autointel.exception.RecursoNaoEncontradoException;
import br.com.fiap.autointel.security.UsuarioAutenticado;
import br.com.fiap.autointel.web.dto.ConsultaRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Núcleo da solução: recebe marca/modelo/versão + lista livre de atributos e produz a lista padronizada.
 * <ol>
 *     <li>Localiza o veículo (busca tolerante a acentos, caixa e espaços);</li>
 *     <li>Traduz cada termo livre para um atributo canônico ({@link ReconhecedorAtributos});</li>
 *     <li>Gera uma linha por termo, SEMPRE com os mesmos campos, marcando explicitamente o que não existe.</li>
 * </ol>
 */
@Service
public class ConsultaService {

    private final ConsultaRepository consultas;
    private final VeiculoRepository veiculos;
    private final AtributoRepository atributos;
    private final ReconhecedorAtributos reconhecedor;

    public ConsultaService(ConsultaRepository consultas, VeiculoRepository veiculos, AtributoRepository atributos,
                           ReconhecedorAtributos reconhecedor) {
        this.consultas = consultas;
        this.veiculos = veiculos;
        this.atributos = atributos;
        this.reconhecedor = reconhecedor;
    }

    @Transactional
    public Consulta realizar(ConsultaRequest request, UsuarioAutenticado usuario) {
        Optional<Veiculo> veiculo = veiculos.findByChaveBusca(
                Veiculo.gerarChave(request.marca(), request.modelo(), request.versao()));
        ReconhecedorAtributos.Indice indice = reconhecedor.indexar(atributos.findAll());

        Consulta consulta = new Consulta(request.marca(), request.modelo(), request.versao(), veiculo.orElse(null),
                usuario.email());

        Set<String> termosProcessados = new HashSet<>();
        for (String termo : request.atributos()) {
            if (!termosProcessados.add(NormalizadorTexto.normalizar(termo))) {
                continue; // ignora termos repetidos mantendo a primeira ocorrência
            }
            consulta.adicionarItem(montarItem(termo.trim(), indice.reconhecer(termo), veiculo));
        }
        return consultas.save(consulta);
    }

    private ConsultaItem montarItem(String termo, Optional<Atributo> atributo, Optional<Veiculo> veiculo) {
        if (atributo.isEmpty()) {
            return new ConsultaItem(termo, null, null, null, null, null, null,
                    StatusEspecificacao.ATRIBUTO_NAO_RECONHECIDO);
        }
        Atributo a = atributo.get();
        Optional<Especificacao> especificacao = veiculo.flatMap(v -> v.especificacaoDe(a.getCodigo()));
        return especificacao
                .map(e -> new ConsultaItem(termo, a.getCodigo(), a.getNome(), a.getCategoria(), e.getValor(),
                        a.getUnidade(), e.getFonte(), StatusEspecificacao.DISPONIVEL))
                .orElseGet(() -> new ConsultaItem(termo, a.getCodigo(), a.getNome(), a.getCategoria(), null,
                        a.getUnidade(), null, StatusEspecificacao.NAO_DISPONIVEL));
    }

    @Transactional(readOnly = true)
    public Consulta buscar(Long id, UsuarioAutenticado usuario) {
        Consulta consulta = consultas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta " + id + " não encontrada"));
        if (!usuario.isAdmin() && !consulta.getSolicitante().equalsIgnoreCase(usuario.email())) {
            throw new AcessoNegadoException("Esta consulta pertence a outro usuário.");
        }
        consulta.getItens().size();
        if (consulta.getVeiculo() != null) {
            consulta.getVeiculo().getMarca();
        }
        return consulta;
    }

    /** ADMIN vê todas as consultas; ANALISTA apenas as próprias. */
    @Transactional(readOnly = true)
    public Page<Consulta> listar(UsuarioAutenticado usuario, Pageable pageable) {
        Page<Consulta> pagina = usuario.isAdmin()
                ? consultas.findAll(pageable)
                : consultas.findBySolicitanteIgnoreCase(usuario.email(), pageable);
        pagina.forEach(c -> c.getItens().size());
        return pagina;
    }

    @Transactional
    public void remover(Long id, UsuarioAutenticado usuario) {
        consultas.delete(buscar(id, usuario));
    }
}
