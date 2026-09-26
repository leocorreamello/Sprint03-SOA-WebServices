package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.Atributo;
import br.com.fiap.autointel.domain.model.CategoriaAtributo;

import java.util.Set;
import java.util.TreeSet;

public record AtributoResponse(String codigo, String nome, CategoriaAtributo categoria, String categoriaDescricao,
                               String unidade, Set<String> sinonimos) {

    public static AtributoResponse de(Atributo a) {
        return new AtributoResponse(a.getCodigo(), a.getNome(), a.getCategoria(), a.getCategoria().getDescricao(),
                a.getUnidade(), new TreeSet<>(a.getSinonimos()));
    }
}
