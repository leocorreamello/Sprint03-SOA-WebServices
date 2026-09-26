package br.com.fiap.autointel.web.controller;

import br.com.fiap.autointel.web.dto.ConsultaResponse;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Exporta a lista padronizada em CSV (separador ';', compatível com Excel pt-BR). */
final class CsvConsultaWriter {

    private static final String CABECALHO =
            "ordem;termo_solicitado;codigo;atributo;categoria;valor;unidade;valor_formatado;status;fonte";

    private CsvConsultaWriter() {
    }

    static String escrever(ConsultaResponse consulta) {
        String linhas = consulta.especificacoes().stream()
                .map(i -> Stream.of(String.valueOf(i.ordem()), i.termoSolicitado(), i.codigo(), i.atributo(),
                                i.categoria() == null ? null : i.categoria().name(), i.valor(), i.unidade(),
                                i.valorFormatado(), i.status().name(), i.fonte())
                        .map(CsvConsultaWriter::celula)
                        .collect(Collectors.joining(";")))
                .collect(Collectors.joining("\n"));
        return CABECALHO + "\n" + linhas + "\n";
    }

    private static String celula(String valor) {
        String v = Objects.toString(valor, "");
        if (v.contains(";") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
