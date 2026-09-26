package br.com.fiap.autointel.web.dto;

import br.com.fiap.autointel.domain.model.Veiculo;

public record VeiculoResponse(Long id, String marca, String modelo, String versao, Integer anoModelo,
                              int totalEspecificacoes) {

    public static VeiculoResponse de(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getMarca(), v.getModelo(), v.getVersao(), v.getAnoModelo(),
                v.getEspecificacoes().size());
    }
}
