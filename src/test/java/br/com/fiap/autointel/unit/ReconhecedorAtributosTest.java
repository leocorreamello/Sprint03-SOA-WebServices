package br.com.fiap.autointel.unit;

import br.com.fiap.autointel.domain.NormalizadorTexto;
import br.com.fiap.autointel.domain.model.Atributo;
import br.com.fiap.autointel.domain.model.CategoriaAtributo;
import br.com.fiap.autointel.service.ReconhecedorAtributos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReconhecedorAtributos (motor de regras)")
class ReconhecedorAtributosTest {

    private ReconhecedorAtributos.Indice indice;

    @BeforeEach
    void setUp() {
        List<Atributo> catalogo = List.of(
                new Atributo("POTENCIA", "Potência máxima", CategoriaAtributo.MOTOR, "cv", Set.of("potência", "hp", "cv")),
                new Atributo("TRANSMISSAO", "Transmissão", CategoriaAtributo.TRANSMISSAO, null, Set.of("câmbio", "gearbox")),
                new Atributo("ALTURA_SOLO", "Altura livre do solo", CategoriaAtributo.DIMENSOES, "mm", Set.of("vão livre")));
        indice = new ReconhecedorAtributos().indexar(catalogo);
    }

    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource({
            "POTENCIA, POTENCIA",           // código
            "Potência máxima, POTENCIA",    // nome
            "potencia maxima, POTENCIA",    // nome sem acento
            "HP, POTENCIA",                 // sinônimo
            "Câmbio, TRANSMISSAO",          // sinônimo com acento
            "CAMBIO, TRANSMISSAO",          // sinônimo sem acento/caixa alta
            "Vão Livre, ALTURA_SOLO",
            "altura_solo, ALTURA_SOLO"
    })
    void deveReconhecerPorCodigoNomeOuSinonimo(String termo, String codigoEsperado) {
        assertThat(indice.reconhecer(termo)).get().extracting(Atributo::getCodigo).isEqualTo(codigoEsperado);
    }

    @ParameterizedTest(name = "erro de digitação \"{0}\"")
    @ValueSource(strings = {"potencai", "transmisão", "altura livre do sollo"})
    void deveTolerarErrosDeDigitacao(String termo) {
        assertThat(indice.reconhecer(termo)).isPresent();
    }

    @ParameterizedTest(name = "\"{0}\" não é reconhecido")
    @ValueSource(strings = {"cafeteira", "teto solar", "xyz", "  ", "cc"})
    void naoDeveReconhecerTermosForaDoCatalogo(String termo) {
        assertThat(indice.reconhecer(termo)).isEmpty();
    }

    @Test
    @DisplayName("O catálogo de carga não possui termos ambíguos (mesmo termo em dois atributos)")
    void catalogoDeCargaNaoPossuiTermosAmbiguos() throws Exception {
        JsonNode atributos;
        try (InputStream in = getClass().getResourceAsStream("/seed/atributos.json")) {
            atributos = JsonMapper.builder().build().readTree(in);
        }
        Map<String, String> donoDoTermo = new HashMap<>();
        List<String> conflitos = new ArrayList<>();
        for (JsonNode a : atributos) {
            String codigo = a.get("codigo").asString();
            Set<String> termos = new HashSet<>();
            termos.add(NormalizadorTexto.normalizar(codigo));
            termos.add(NormalizadorTexto.normalizar(a.get("nome").asString()));
            a.get("sinonimos").forEach(s -> termos.add(NormalizadorTexto.normalizar(s.asString())));
            for (String termo : termos) {
                String anterior = donoDoTermo.putIfAbsent(termo, codigo);
                if (anterior != null) {
                    conflitos.add("'" + termo + "' em " + anterior + " e " + codigo);
                }
            }
        }
        assertThat(conflitos).isEmpty();
    }
}
