package br.com.fiap.autointel.service;

import br.com.fiap.autointel.domain.NormalizadorTexto;
import br.com.fiap.autointel.domain.model.Atributo;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Motor de regras que traduz o termo livre digitado pelo usuário para um atributo canônico do catálogo.
 * <ol>
 *     <li><b>Correspondência exata</b> (após normalização) com código, nome ou qualquer sinônimo;</li>
 *     <li><b>Correspondência aproximada</b> (Levenshtein) para tolerar erros de digitação,
 *     aplicada apenas a termos com 4+ caracteres e similaridade mínima de {@value #SIMILARIDADE_MINIMA}.</li>
 * </ol>
 * Se nenhuma regra casar, o termo é devolvido como "não reconhecido" — nunca é descartado da saída.
 */
@Component
public class ReconhecedorAtributos {

    static final double SIMILARIDADE_MINIMA = 0.85;
    private static final int TAMANHO_MINIMO_APROXIMACAO = 4;

    /** Índice imutável "termo normalizado → atributo" para um catálogo. */
    public Indice indexar(Collection<Atributo> catalogo) {
        Map<String, Atributo> chaves = new LinkedHashMap<>();
        for (Atributo atributo : catalogo) {
            chaves.putIfAbsent(NormalizadorTexto.normalizar(atributo.getCodigo()), atributo);
            chaves.putIfAbsent(NormalizadorTexto.normalizar(atributo.getNome()), atributo);
            atributo.getSinonimos().forEach(s -> chaves.putIfAbsent(NormalizadorTexto.normalizar(s), atributo));
        }
        return new Indice(Map.copyOf(chaves));
    }

    public static final class Indice {

        private final Map<String, Atributo> chaves;

        private Indice(Map<String, Atributo> chaves) {
            this.chaves = chaves;
        }

        public Optional<Atributo> reconhecer(String termo) {
            String normalizado = NormalizadorTexto.normalizar(termo);
            if (normalizado.isEmpty()) {
                return Optional.empty();
            }
            Atributo exato = chaves.get(normalizado);
            if (exato != null) {
                return Optional.of(exato);
            }
            if (normalizado.length() < TAMANHO_MINIMO_APROXIMACAO) {
                return Optional.empty();
            }
            Atributo melhor = null;
            double melhorScore = 0;
            for (Map.Entry<String, Atributo> entrada : chaves.entrySet()) {
                double score = NormalizadorTexto.similaridade(normalizado, entrada.getKey());
                if (score > melhorScore) {
                    melhorScore = score;
                    melhor = entrada.getValue();
                }
            }
            return melhorScore >= SIMILARIDADE_MINIMA ? Optional.of(melhor) : Optional.empty();
        }
    }
}
