package br.com.fiap.autointel.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Autenticação e cadastro de usuários")
class AutenticacaoIT extends ApiTestSupport {

    @Test
    @DisplayName("POST /auth/login com credenciais válidas → 200 + JWT")
    void loginComSucesso() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"analista@autointel.com\",\"senha\":\"Analista@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token", matchesPattern("^[\\w-]+\\.[\\w-]+\\.[\\w-]+$")))
                .andExpect(jsonPath("$.expiraEmSegundos").value(3600))
                .andExpect(jsonPath("$.expiraEm").exists())
                .andExpect(jsonPath("$.usuario.perfil").value("ANALISTA"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    @DisplayName("POST /auth/login com senha errada → 401 padronizado")
    void loginSenhaErrada() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"analista@autointel.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    @DisplayName("POST /auth/login bloqueia a sexta falha da mesma origem e conta → 429")
    void limitaTentativasDeLogin() throws Exception {
        String email = unico("inexistente") + "@teste.com";
        String body = "{\"email\":\"" + email + "\",\"senha\":\"incorreta\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"))
                .andExpect(jsonPath("$.codigo").value("LIMITE_LOGIN"));
    }

    @Test
    @DisplayName("POST /auth/login com usuário inexistente → 401 (sem revelar se o e-mail existe)")
    void loginUsuarioInexistente() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@teste.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    @DisplayName("POST /auth/login com corpo inválido → 400 com lista de erros por campo")
    void loginCorpoInvalido() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-email\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("email", "senha")));
    }

    @Test
    @DisplayName("POST /auth/login com JSON malformado → 400")
    void loginJsonMalformado() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{email:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"));
    }

    @Test
    @DisplayName("POST /usuarios (cadastro público) → 201 + Location, sempre com perfil ANALISTA")
    void cadastroPublico() throws Exception {
        String email = unico("novo") + "@teste.com";
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo\",\"email\":\"" + email + "\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/usuarios/\\d+$")))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.perfil").value("ANALISTA"));
    }

    @Test
    @DisplayName("POST /usuarios com e-mail já cadastrado → 409")
    void cadastroDuplicado() throws Exception {
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"ANALISTA@autointel.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

    @Test
    @DisplayName("POST /usuarios com senha fraca → 400")
    void cadastroSenhaFraca() throws Exception {
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"fraca@teste.com\",\"senha\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", everyItem(is("senha"))));
    }

    @Test
    @DisplayName("GET /auth/me devolve as informações contidas no token")
    void meComToken() throws Exception {
        mvc.perform(autenticado(get("/api/v1/auth/me"), tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.perfil").value("ADMIN"))
                .andExpect(jsonPath("$.tokenEmitidoEm").exists())
                .andExpect(jsonPath("$.tokenExpiraEm").exists());
    }

    @Test
    @DisplayName("GET /auth/me sem token → 401")
    void meSemToken() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }
}
