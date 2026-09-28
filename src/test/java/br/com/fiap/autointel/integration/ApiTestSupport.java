package br.com.fiap.autointel.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Infraestrutura comum: contexto Spring completo (com Spring Security real) + MockMvc. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestSupport {

    protected static final String ADMIN_EMAIL = "admin@autointel.com";
    protected static final String ADMIN_SENHA = "Admin@123";
    protected static final String ANALISTA_EMAIL = "analista@autointel.com";
    protected static final String ANALISTA_SENHA = "Analista@123";

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JsonMapper json;

    protected String login(String email, String senha) throws Exception {
        String resposta = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Credenciais(email, senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("token").asString();
    }

    protected String tokenAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_SENHA);
    }

    protected String tokenAnalista() throws Exception {
        return login(ANALISTA_EMAIL, ANALISTA_SENHA);
    }

    /** Cria um novo analista (e-mail único) e devolve o token dele. */
    protected String tokenNovoAnalista() throws Exception {
        String email = "analista" + SEQUENCIA.incrementAndGet() + "-" + System.nanoTime() + "@teste.com";
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Analista Teste","email":"%s","senha":"Senha@123"}""".formatted(email)))
                .andExpect(status().isCreated());
        return login(email, "Senha@123");
    }

    protected static MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder req, String token) {
        return req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected static String unico(String prefixo) {
        return prefixo + "-" + SEQUENCIA.incrementAndGet() + "-" + System.nanoTime();
    }

    protected JsonNode corpo(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString());
    }

    record Credenciais(String email, String senha) {
    }
}
