package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.model.Atributo;
import br.com.fiap.autointel.domain.model.CategoriaAtributo;
import br.com.fiap.autointel.domain.repository.AtributoRepository;
import br.com.fiap.autointel.exception.ConflitoException;
import br.com.fiap.autointel.exception.RecursoNaoEncontradoException;
import br.com.fiap.autointel.exception.RequisicaoInvalidaException;
import br.com.fiap.autointel.web.dto.AtributoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AtributoService {

    private final AtributoRepository atributos;

    public AtributoService(AtributoRepository atributos) {
        this.atributos = atributos;
    }

    @Transactional(readOnly = true)
    public List<Atributo> listar(CategoriaAtributo categoria) {
        return categoria == null
                ? atributos.findAllByOrderByCategoriaAscNomeAsc()
                : atributos.findByCategoriaOrderByNomeAsc(categoria);
    }

    @Transactional(readOnly = true)
    public Atributo buscar(String codigo) {
        return atributos.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atributo '" + codigo + "' não encontrado"));
    }

    @Transactional
    public Atributo criar(AtributoRequest request) {
        if (request.codigo() == null || request.codigo().isBlank()) {
            throw new RequisicaoInvalidaException("codigo", "é obrigatório na criação do atributo");
        }
        if (atributos.existsByCodigo(request.codigo())) {
            throw new ConflitoException("Já existe um atributo com o código " + request.codigo());
        }
        return atributos.save(new Atributo(request.codigo(), request.nome().trim(), request.categoria(),
                request.unidade(), request.sinonimos()));
    }

    @Transactional
    public Atributo atualizar(String codigo, AtributoRequest request) {
        Atributo atributo = buscar(codigo);
        atributo.atualizar(request.nome().trim(), request.categoria(), request.unidade(), request.sinonimos());
        return atributo;
    }

    @Transactional
    public void remover(String codigo) {
        Atributo atributo = buscar(codigo);
        if (atributos.estaEmUso(atributo)) {
            throw new ConflitoException("O atributo " + atributo.getCodigo()
                    + " possui especificações cadastradas e não pode ser removido");
        }
        atributos.delete(atributo);
    }
}
