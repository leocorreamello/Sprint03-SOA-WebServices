package br.com.fiap.autointel.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Padronização das respostas de erro (RFC 9457)")
class TratamentoErrosIT extends ApiTestSupport {

    @Test
    @DisplayName("Todo erro traz type, title, status, detail, instance, codigo e timestamp")
    void camposPadrao() throws Exception {
        mvc.perform(autenticado(get("/api/v1/veiculos/999999"), tokenAnalista()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://autointel.fiap.com.br/erros/recurso-nao-encontrado"))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Veículo 999999 não encontrado"))
                .andExpect(jsonPath("$.instance").value("/api/v1/veiculos/999999"))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Parâmetro de caminho com tipo inválido → 400")
    void tipoInvalido() throws Exception {
        mvc.perform(autenticado(get("/api/v1/veiculos/abc"), tokenAnalista()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @Test
    @DisplayName("Método HTTP não suportado → 405 no mesmo formato")
    void metodoNaoSuportado() throws Exception {
        mvc.perform(autenticado(patch("/api/v1/veiculos/1"), tokenAdmin()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("METODO_NAO_SUPORTADO"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Content-Type não suportado → 415 no mesmo formato")
    void midiaNaoSuportada() throws Exception {
        mvc.perform(autenticado(post("/api/v1/consultas"), tokenAnalista())
                        .contentType(MediaType.TEXT_PLAIN).content("oi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("TIPO_DE_MIDIA_NAO_SUPORTADO"));
    }

    @Test
    @DisplayName("Rota inexistente (autenticado) → 404 no mesmo formato")
    void rotaInexistente() throws Exception {
        mvc.perform(autenticado(get("/api/v1/nao-existe"), tokenAnalista()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }
}
