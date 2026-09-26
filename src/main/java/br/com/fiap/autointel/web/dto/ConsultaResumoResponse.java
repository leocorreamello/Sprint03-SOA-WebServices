package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.Consulta;
import br.com.fiap.autointel.domain.model.StatusEspecificacao;

import java.time.Instant;

public record ConsultaResumoResponse(Long id, String marca, String modelo, String versao, boolean veiculoEncontrado,
                                     int totalSolicitados, long disponiveis, Instant realizadaEm, String solicitante) {

    public static ConsultaResumoResponse de(Consulta c) {
        return new ConsultaResumoResponse(c.getId(), c.getMarca(), c.getModelo(), c.getVersao(), c.getVeiculo() != null,
                c.getItens().size(), c.contar(StatusEspecificacao.DISPONIVEL), c.getRealizadaEm(), c.getSolicitante());
    }
}
