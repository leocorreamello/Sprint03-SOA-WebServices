package br.com.fiap.autointel.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validação exigida pelo desafio: a solução deve entregar corretamente as especificações da
 * Ford Ranger Raptor, de forma clara, organizada e consistente.
 * O usuário descreve os atributos com as próprias palavras (sinônimos, sem acento, caixa variada).
 */
@DisplayName("Validação do desafio: Ford Ranger Raptor")
class RangerRaptorValidacaoIT extends ApiTestSupport {

    /** termo livre digitado pelo usuário → valor formatado esperado na saída padronizada. */
    private static final Map<String, String> ESPERADO = new LinkedHashMap<>();

    static {
        ESPERADO.put("Preço", "R$ 424.990");
        ESPERADO.put("Motor", "3.0 V6 EcoBoost biturbo");
        ESPERADO.put("cilindrada", "2.956 cm³");
        ESPERADO.put("Combustível", "Gasolina");
        ESPERADO.put("Potência", "397 cv");
        ESPERADO.put("TORQUE", "59,4 kgfm");
        ESPERADO.put("Câmbio", "Automática de 10 velocidades");
        ESPERADO.put("tracao", "4x4 com seleção eletrônica (4x2, 4x4 Auto, 4x4 High e 4x4 Low)");
        ESPERADO.put("Reduzida", "Sim");
        ESPERADO.put("Bloqueio de diferencial", "Dianteiro e traseiro, com acionamento eletrônico");
        ESPERADO.put("Modos de condução", "7 modos: Normal, Sport, Escorregadio, Lama/Sulcos, Areia, Pedras e Baja");
        ESPERADO.put("Suspensão dianteira", "Independente, com braços superiores e inferiores em alumínio");
        ESPERADO.put("Suspensão traseira", "Eixo rígido com molas helicoidais e barra Watt's link");
        ESPERADO.put("Amortecedores FOX", "FOX 2.5\" Live Valve com bypass interno e controle eletrônico");
        ESPERADO.put("Rodas", "Liga leve aro 17\"");
        ESPERADO.put("Pneus", "BFGoodrich All-Terrain T/A KO2 285/70 R17");
        ESPERADO.put("Comprimento", "5.381 mm");
        ESPERADO.put("Largura", "2.028 mm");
        ESPERADO.put("Altura", "1.922 mm");
        ESPERADO.put("Entre-eixos", "3.270 mm");
        ESPERADO.put("vão livre do solo", "272 mm");
        ESPERADO.put("Ângulo de ataque", "32°");
        ESPERADO.put("Ângulo de saída", "27°");
        ESPERADO.put("Ângulo de rampa", "24°");
        ESPERADO.put("Capacidade de imersão", "850 mm");
        ESPERADO.put("Tanque", "80 L");
        ESPERADO.put("Capacidade de reboque", "2.500 kg");
        ESPERADO.put("Airbags", "9");
        ESPERADO.put("Piloto automático adaptativo", "Sim, com função Stop & Go");
        ESPERADO.put("Câmera 360", "360°");
        ESPERADO.put("Central multimídia", "SYNC 4 com tela vertical de 12\"");
        ESPERADO.put("Painel digital", "Digital de 12,4\"");
        ESPERADO.put("Som", "Bang & Olufsen com 10 alto-falantes");
        ESPERADO.put("Faróis", "Full LED Matrix com assinatura em \"C\"");
        ESPERADO.put("Escapamento ativo", "Ativo com 4 modos (Silencioso, Normal, Esportivo e Baja)");
    }

    private static final Set<String> CAMPOS_PADRAO = new TreeSet<>(List.of("ordem", "termoSolicitado", "codigo",
            "atributo", "categoria", "valor", "unidade", "valorFormatado", "fonte", "status"));

    private JsonNode consultar(String marca, String modelo, String versao, List<String> atributos) throws Exception {
        String body = json.writeValueAsString(Map.of("marca", marca, "modelo", modelo, "versao", versao,
                "atributos", atributos));
        return corpo(mvc.perform(autenticado(post("/api/v1/consultas"), tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()));
    }

    @Test
    @DisplayName("Entrega todas as especificações da Ranger Raptor, na ordem pedida e com status DISPONIVEL")
    void entregaTodasAsEspecificacoesDaRangerRaptor() throws Exception {
        List<String> termos = List.copyOf(ESPERADO.keySet());
        JsonNode resposta = consultar("Ford", "Ranger", "Raptor", termos);

        assertThat(resposta.get("veiculo").get("encontrado").asBoolean()).isTrue();
        assertThat(resposta.get("resumo").get("totalSolicitados").asInt()).isEqualTo(termos.size());
        assertThat(resposta.get("resumo").get("disponiveis").asInt()).isEqualTo(termos.size());

        JsonNode itens = resposta.get("especificacoes");
        assertThat(itens.size()).isEqualTo(termos.size());
        for (int i = 0; i < termos.size(); i++) {
            JsonNode item = itens.get(i);
            String termo = termos.get(i);
            assertThat(item.get("ordem").asInt()).isEqualTo(i + 1);
            assertThat(item.get("termoSolicitado").asString()).isEqualTo(termo);
            assertThat(item.get("status").asString()).as("status de '%s'", termo).isEqualTo("DISPONIVEL");
            assertThat(item.get("valorFormatado").asString()).as("valor de '%s'", termo).isEqualTo(ESPERADO.get(termo));
        }
    }

    @Test
    @DisplayName("Busca do veículo é tolerante a caixa, acentos e espaços")
    void buscaTolerante() throws Exception {
        JsonNode resposta = consultar("  FORD ", "ranger", "RAPTOR", List.of("potencia"));
        assertThat(resposta.get("veiculo").get("encontrado").asBoolean()).isTrue();
        assertThat(resposta.get("especificacoes").get(0).get("valor").asString()).isEqualTo("397");
    }

    @Test
    @DisplayName("Formato é idêntico para qualquer veículo; ausência de dados fica explícita")
    void formatoPadronizadoParaQualquerVeiculo() throws Exception {
        List<String> termos = List.of("Potência", "Torque", "Teto solar", "Cafeteira expressa");
        JsonNode raptor = consultar("Ford", "Ranger", "Raptor", termos);
        JsonNode hilux = consultar("Toyota", "Hilux", "GR-Sport", termos);
        JsonNode inexistente = consultar("Marca", "Inexistente", "XYZ", termos);

        for (JsonNode resposta : List.of(raptor, hilux, inexistente)) {
            assertThat(nomesDosCampos(resposta)).containsExactlyInAnyOrder(
                    "id", "veiculo", "realizadaEm", "solicitante", "resumo", "especificacoes");
            assertThat(resposta.get("especificacoes").size()).isEqualTo(termos.size());
            resposta.get("especificacoes").forEach(item ->
                    assertThat(new TreeSet<>(nomesDosCampos(item))).isEqualTo(CAMPOS_PADRAO));
        }

        // Hilux: dado existente x dado ausente
        JsonNode itensHilux = hilux.get("especificacoes");
        assertThat(itensHilux.get(0).get("valorFormatado").asString()).isEqualTo("224 cv");
        assertThat(itensHilux.get(2).get("status").asString()).isEqualTo("NAO_DISPONIVEL");
        assertThat(itensHilux.get(2).get("valor").isNull()).isTrue();
        assertThat(itensHilux.get(2).get("valorFormatado").asString()).isEqualTo("Não disponível");
        assertThat(itensHilux.get(3).get("status").asString()).isEqualTo("ATRIBUTO_NAO_RECONHECIDO");

        // Veículo inexistente: mesma estrutura, tudo marcado como não disponível
        assertThat(inexistente.get("veiculo").get("encontrado").asBoolean()).isFalse();
        assertThat(inexistente.get("resumo").get("disponiveis").asInt()).isZero();
        assertThat(inexistente.get("especificacoes").get(0).get("codigo").asString()).isEqualTo("POTENCIA");
        assertThat(inexistente.get("especificacoes").get(0).get("status").asString()).isEqualTo("NAO_DISPONIVEL");
    }

    @Test
    @DisplayName("Termos repetidos (mesmo após normalização) geram uma única linha")
    void termosRepetidos() throws Exception {
        JsonNode resposta = consultar("Ford", "Ranger", "Raptor", List.of("Potência", "POTENCIA", "potência "));
        assertThat(resposta.get("especificacoes").size()).isEqualTo(1);
    }

    private static List<String> nomesDosCampos(JsonNode node) {
        return node.propertyNames().stream().toList();
    }
}
