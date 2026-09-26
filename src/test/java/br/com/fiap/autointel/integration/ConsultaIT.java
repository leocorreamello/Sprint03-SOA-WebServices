package br.com.fiap.autointel.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Recurso /consultas")
class ConsultaIT extends ApiTestSupport {

    private static final String CONSULTA = """
            {"marca":"Ford","modelo":"Ranger","versao":"Raptor","atributos":["Potência","Torque"]}""";

    private long criarConsulta(String token) throws Exception {
        return corpo(mvc.perform(autenticado(post("/api/v1/consultas"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(CONSULTA))
                .andExpect(status().isCreated()))
                .get("id").asLong();
    }

    @Test
    @DisplayName("POST → 201 com header Location apontando para a consulta criada")
    void criarConsulta() throws Exception {
        mvc.perform(autenticado(post("/api/v1/consultas"), tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON).content(CONSULTA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/consultas/\\d+$")))
                .andExpect(jsonPath("$.solicitante").value(ANALISTA_EMAIL))
                .andExpect(jsonPath("$.especificacoes[0].valorFormatado").value("397 cv"))
                .andExpect(jsonPath("$.especificacoes[1].valorFormatado").value("59,4 kgfm"));
    }

    @Test
    @DisplayName("POST sem token → 401")
    void criarSemToken() throws Exception {
        mvc.perform(post("/api/v1/consultas").contentType(MediaType.APPLICATION_JSON).content(CONSULTA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST com campos obrigatórios ausentes → 400 com erros por campo (em português)")
    void criarInvalida() throws Exception {
        mvc.perform(autenticado(post("/api/v1/consultas"), tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"marca\":\"\",\"atributos\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("marca", "modelo", "versao", "atributos")))
                .andExpect(jsonPath("$.erros[?(@.campo == 'marca')].mensagem", hasItem("não deve estar em branco")));
    }

    @Test
    @DisplayName("GET /{id} pelo dono → 200; por outro analista → 403; pelo ADMIN → 200")
    void controleDeAcessoPorDono() throws Exception {
        String dono = tokenNovoAnalista();
        long id = criarConsulta(dono);

        mvc.perform(autenticado(get("/api/v1/consultas/" + id), dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.especificacoes", hasSize(2)));
        mvc.perform(autenticado(get("/api/v1/consultas/" + id), tokenNovoAnalista()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Esta consulta pertence a outro usuário."));
        mvc.perform(autenticado(get("/api/v1/consultas/" + id), tokenAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /{id} inexistente → 404")
    void consultaInexistente() throws Exception {
        mvc.perform(autenticado(get("/api/v1/consultas/999999"), tokenAnalista()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /{id} com Accept: text/csv exporta a lista padronizada")
    void exportarCsv() throws Exception {
        String token = tokenNovoAnalista();
        long id = criarConsulta(token);
        mvc.perform(autenticado(get("/api/v1/consultas/" + id), token).accept("text/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(startsWith("ordem;termo_solicitado;codigo;atributo;categoria;valor")))
                .andExpect(content().string(containsString("1;Potência;POTENCIA;Potência máxima;MOTOR;397;cv;397 cv;DISPONIVEL")));
    }

    @Test
    @DisplayName("GET lista paginada: analista vê apenas as próprias consultas")
    void listarHistorico() throws Exception {
        String token = tokenNovoAnalista();
        criarConsulta(token);
        criarConsulta(token);
        mvc.perform(autenticado(get("/api/v1/consultas?size=10"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.tamanho").value(10));
    }

    @Test
    @DisplayName("DELETE pelo dono → 204 e depois GET → 404; outro analista → 403")
    void removerConsulta() throws Exception {
        String dono = tokenNovoAnalista();
        long id = criarConsulta(dono);

        mvc.perform(autenticado(delete("/api/v1/consultas/" + id), tokenNovoAnalista()))
                .andExpect(status().isForbidden());
        mvc.perform(autenticado(delete("/api/v1/consultas/" + id), dono))
                .andExpect(status().isNoContent());
        mvc.perform(autenticado(get("/api/v1/consultas/" + id), dono))
                .andExpect(status().isNotFound());
    }
}
