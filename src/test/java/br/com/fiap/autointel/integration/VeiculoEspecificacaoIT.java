package br.com.fiap.autointel.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Recursos /veiculos e /veiculos/{id}/especificacoes")
class VeiculoEspecificacaoIT extends ApiTestSupport {

    private long criarVeiculo(String token, String versao) throws Exception {
        return corpo(mvc.perform(autenticado(post("/api/v1/veiculos"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"Chevrolet\",\"modelo\":\"S10\",\"versao\":\"" + versao + "\",\"anoModelo\":2025}"))
                .andExpect(status().isCreated()))
                .get("id").asLong();
    }

    @Test
    @DisplayName("Ciclo completo do ADMIN: POST 201 → GET 200 → PUT 200 → DELETE 204 → GET 404")
    void cicloCompletoVeiculo() throws Exception {
        String admin = tokenAdmin();
        String versao = unico("ZR2");

        String location = mvc.perform(autenticado(post("/api/v1/veiculos"), admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"Chevrolet\",\"modelo\":\"S10\",\"versao\":\"" + versao + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.totalEspecificacoes").value(0))
                .andReturn().getResponse().getHeader("Location");
        String caminho = location.substring(location.indexOf("/api/"));

        mvc.perform(autenticado(get(caminho), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(versao));
        mvc.perform(autenticado(put(caminho), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"Chevrolet\",\"modelo\":\"S10\",\"versao\":\"" + versao + "\",\"anoModelo\":2026}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anoModelo").value(2026));
        mvc.perform(autenticado(delete(caminho), admin)).andExpect(status().isNoContent());
        mvc.perform(autenticado(get(caminho), admin)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST de veículo já existente (ignorando caixa/acentos) → 409")
    void veiculoDuplicado() throws Exception {
        mvc.perform(autenticado(post("/api/v1/veiculos"), tokenAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"FORD\",\"modelo\":\"ranger\",\"versao\":\"Raptor\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST de veículo com campos inválidos → 400")
    void veiculoInvalido() throws Exception {
        mvc.perform(autenticado(post("/api/v1/veiculos"), tokenAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"\",\"modelo\":\"S10\",\"versao\":\"LTZ\",\"anoModelo\":1800}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("marca", "anoModelo")));
    }

    @Test
    @DisplayName("GET com filtro por marca")
    void filtrarPorMarca() throws Exception {
        mvc.perform(autenticado(get("/api/v1/veiculos?marca=ford"), tokenAnalista()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].marca", everyItem(is("Ford"))))
                .andExpect(jsonPath("$[?(@.versao == 'Raptor')].totalEspecificacoes", hasItem(greaterThan(30))));
    }

    @Test
    @DisplayName("Especificações: PUT cria (201), PUT de novo substitui (200), GET, DELETE (204)")
    void cicloEspecificacao() throws Exception {
        String admin = tokenAdmin();
        long id = criarVeiculo(admin, unico("LTZ"));
        String url = "/api/v1/veiculos/" + id + "/especificacoes/POTENCIA";

        mvc.perform(autenticado(put(url), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":\"200\",\"fonte\":\"teste\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorFormatado").value("200 cv"));
        mvc.perform(autenticado(put(url), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":\"207\",\"fonte\":\"teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("207"));
        mvc.perform(autenticado(get("/api/v1/veiculos/" + id + "/especificacoes"), tokenAnalista()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoAtributo").value("POTENCIA"));
        mvc.perform(autenticado(get(url), tokenAnalista()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorFormatado").value("207 cv"));
        mvc.perform(autenticado(delete(url), admin)).andExpect(status().isNoContent());
        mvc.perform(autenticado(get(url), admin)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Especificação com atributo inexistente → 404; analista tentando escrever → 403")
    void especificacaoErros() throws Exception {
        String admin = tokenAdmin();
        long id = criarVeiculo(admin, unico("High Country"));
        mvc.perform(autenticado(put("/api/v1/veiculos/" + id + "/especificacoes/NAO_EXISTE"), admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"1\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(autenticado(put("/api/v1/veiculos/" + id + "/especificacoes/POTENCIA"), tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Especificação cadastrada via API passa a aparecer nas consultas")
    void especificacaoRefleteNaConsulta() throws Exception {
        String admin = tokenAdmin();
        String versao = unico("WT");
        long id = criarVeiculo(admin, versao);
        mvc.perform(autenticado(put("/api/v1/veiculos/" + id + "/especificacoes/TORQUE"), admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"51\"}"))
                .andExpect(status().isCreated());

        mvc.perform(autenticado(post("/api/v1/consultas"), tokenAnalista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"Chevrolet\",\"modelo\":\"S10\",\"versao\":\"" + versao
                                + "\",\"atributos\":[\"torque\",\"potência\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.especificacoes[0].valorFormatado").value("51 kgfm"))
                .andExpect(jsonPath("$.especificacoes[1].status").value("NAO_DISPONIVEL"));
    }
}
