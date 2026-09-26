package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.*;
import br.com.fiap.autointel.service.FormatadorValor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Saída padronizada: a estrutura é idêntica para qualquer veículo e todas as linhas
 * possuem exatamente os mesmos campos (valores ausentes vêm como {@code null} + status explícito).
 */
public record ConsultaResponse(Long id, VeiculoConsultado veiculo, Instant realizadaEm, String solicitante,
                               Resumo resumo, List<Item> especificacoes) {

    public record VeiculoConsultado(
            @Schema(description = "Indica se o veículo existe na base de dados") boolean encontrado,
            Long veiculoId, String marca, String modelo, String versao, Integer anoModelo) {
    }

    public record Resumo(int totalSolicitados, long disponiveis, long naoDisponiveis, long naoReconhecidos) {
    }

    public record Item(int ordem,
                       @Schema(description = "Termo exatamente como enviado pelo usuário") String termoSolicitado,
                       @Schema(description = "Código canônico do atributo (null se não reconhecido)") String codigo,
                       String atributo, CategoriaAtributo categoria, String valor, String unidade,
                       @Schema(example = "397 cv") String valorFormatado, String fonte,
                       StatusEspecificacao status) {
    }

    public static ConsultaResponse de(Consulta c) {
        Veiculo v = c.getVeiculo();
        VeiculoConsultado veiculo = v == null
                ? new VeiculoConsultado(false, null, c.getMarca(), c.getModelo(), c.getVersao(), null)
                : new VeiculoConsultado(true, v.getId(), v.getMarca(), v.getModelo(), v.getVersao(), v.getAnoModelo());
        List<Item> itens = c.getItens().stream()
                .map(i -> new Item(i.getOrdem(), i.getTermoSolicitado(), i.getCodigoAtributo(), i.getNomeAtributo(),
                        i.getCategoria(), i.getValor(), i.getUnidade(),
                        FormatadorValor.formatar(i.getValor(), i.getUnidade(), i.getStatus()), i.getFonte(),
                        i.getStatus()))
                .toList();
        Resumo resumo = new Resumo(itens.size(), c.contar(StatusEspecificacao.DISPONIVEL),
                c.contar(StatusEspecificacao.NAO_DISPONIVEL), c.contar(StatusEspecificacao.ATRIBUTO_NAO_RECONHECIDO));
        return new ConsultaResponse(c.getId(), veiculo, c.getRealizadaEm(), c.getSolicitante(), resumo, itens);
    }
}
