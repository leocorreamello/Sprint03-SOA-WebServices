package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.CategoriaAtributo;
import br.com.fiap.autointel.domain.model.Especificacao;
import br.com.fiap.autointel.service.FormatadorValor;

import java.time.Instant;

public record EspecificacaoResponse(String codigoAtributo, String atributo, CategoriaAtributo categoria,
                                    String valor, String unidade, String valorFormatado, String fonte,
                                    Instant atualizadoEm) {

    public static EspecificacaoResponse de(Especificacao e) {
        var a = e.getAtributo();
        return new EspecificacaoResponse(a.getCodigo(), a.getNome(), a.getCategoria(), e.getValor(), a.getUnidade(),
                FormatadorValor.comUnidade(e.getValor(), a.getUnidade()), e.getFonte(), e.getAtualizadoEm());
    }
}
