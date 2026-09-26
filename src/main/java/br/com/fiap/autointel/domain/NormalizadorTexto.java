package br.com.fiap.autointel.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normaliza textos livres para comparação: remove acentos, converte para minúsculas,
 * troca pontuação por espaço e colapsa espaços. Ex.: " Potência Máx. (cv) " → "potencia max cv".
 */
public final class NormalizadorTexto {

    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");
    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");

    private NormalizadorTexto() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = MARCAS_DIACRITICAS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        return NAO_ALFANUMERICO.matcher(semAcento.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
    }

    /**
     * Similaridade entre 0 e 1 baseada na distância de Damerau-Levenshtein (variante OSA), que tolera
     * erros de digitação comuns: letra trocada, faltando, sobrando ou invertida ("potencai" → "potencia").
     */
    public static double similaridade(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        int maior = Math.max(a.length(), b.length());
        if (maior == 0) {
            return 1.0;
        }
        return 1.0 - (double) distancia(a, b) / maior;
    }

    static int distancia(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int custo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + custo);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[a.length()][b.length()];
    }
}
