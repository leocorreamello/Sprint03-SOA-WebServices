package br.com.fiap.autointel.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Recurso /atributos (catálogo)")
class AtributoIT extends ApiTestSupport {

    @Test
    @DisplayName("GET público lista o catálogo e permite filtrar por categoria")
    void listarPublico() throws Exception {
        mvc.perform(get("/api/v1/atributos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(50))));
        mvc.perform(get("/api/v1/atributos?categoria=MOTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categoria", everyItem(is("MOTOR"))))
                .andExpect(jsonPath("$[*].codigo", hasItems("POTENCIA", "TORQUE")));
    }

    @Test
    @DisplayName("GET /{codigo} público → 200; inexistente → 404; categoria inválida → 400")
    void buscarPorCodigo() throws Exception {
        mvc.perform(get("/api/v1/atributos/potencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade").value("cv"))
                .andExpect(jsonPath("$.sinonimos", hasItem("hp")));
        mvc.perform(get("/api/v1/atributos/NAO_EXISTE")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/atributos?categoria=INVALIDA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @Test
    @DisplayName("ADMIN cria (201), atualiza (200) e remove (204) atributo; novo sinônimo já é reconhecido")
    void cicloAtributo() throws Exception {
        String admin = tokenAdmin();
        String codigo = "TESTE_" + System.nanoTime();

        mvc.perform(autenticado(post("/api/v1/atributos"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"nome\":\"Atributo de teste\",\"categoria\":\"OUTROS\","
                                + "\"sinonimos\":[\"sinonimo exclusivo " + codigo + "\"]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/atributos/" + codigo)));
        mvc.perform(autenticado(post("/api/v1/consultas"), tokenAnalista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"Ford\",\"modelo\":\"Ranger\",\"versao\":\"Raptor\","
                                + "\"atributos\":[\"sinonimo exclusivo " + codigo + "\"]}"))
                .andExpect(jsonPath("$.especificacoes[0].codigo").value(codigo))
                .andExpect(jsonPath("$.especificacoes[0].status").value("NAO_DISPONIVEL"));
        mvc.perform(autenticado(put("/api/v1/atributos/" + codigo), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Renomeado\",\"categoria\":\"CONFORTO\",\"unidade\":\"un\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Renomeado"))
                .andExpect(jsonPath("$.categoria").value("CONFORTO"));
        mvc.perform(autenticado(delete("/api/v1/atributos/" + codigo), admin)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST com código duplicado → 409; sem código → 400")
    void criarInvalido() throws Exception {
        String admin = tokenAdmin();
        mvc.perform(autenticado(post("/api/v1/atributos"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"POTENCIA\",\"nome\":\"x\",\"categoria\":\"MOTOR\"}"))
                .andExpect(status().isConflict());
        mvc.perform(autenticado(post("/api/v1/atributos"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"x\",\"categoria\":\"MOTOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("codigo"));
    }

    @Test
    @DisplayName("DELETE de atributo com especificações cadastradas → 409")
    void removerEmUso() throws Exception {
        mvc.perform(autenticado(delete("/api/v1/atributos/POTENCIA"), tokenAdmin()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST sem token → 401 (escrita no catálogo não é pública)")
    void criarSemToken() throws Exception {
        mvc.perform(post("/api/v1/atributos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"X1\",\"nome\":\"x\",\"categoria\":\"MOTOR\"}"))
                .andExpect(status().isUnauthorized());
    }
}
