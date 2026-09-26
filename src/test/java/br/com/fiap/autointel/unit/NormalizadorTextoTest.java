package br.com.fiap.autointel.unit;

import br.com.fiap.autointel.domain.NormalizadorTexto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NormalizadorTexto")
class NormalizadorTextoTest {

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            " Potência Máx. (cv) |potencia max cv",
            "CÂMBIO|cambio",
            "Ângulo   de   entrada|angulo de entrada",
            "0-100 km/h|0 100 km h",
            "Ford|ford"
    })
    void deveNormalizarAcentosCaixaEPontuacao(String entrada, String esperado) {
        assertThat(NormalizadorTexto.normalizar(entrada)).isEqualTo(esperado);
    }

    @Test
    void deveTratarNuloComoVazio() {
        assertThat(NormalizadorTexto.normalizar(null)).isEmpty();
    }

    @Test
    void similaridadeDeveSerMaximaParaTextosIguais() {
        assertThat(NormalizadorTexto.similaridade("torque", "torque")).isEqualTo(1.0);
    }

    @Test
    void similaridadeDeveTolerarLetrasInvertidas() {
        // "potencai" x "potencia": 1 transposição em 8 caracteres
        assertThat(NormalizadorTexto.similaridade("potencai", "potencia")).isEqualTo(1 - 1.0 / 8);
    }

    @Test
    void similaridadeDeveSerBaixaParaTextosDiferentes() {
        assertThat(NormalizadorTexto.similaridade("altura", "largura")).isLessThan(0.85);
    }
}
