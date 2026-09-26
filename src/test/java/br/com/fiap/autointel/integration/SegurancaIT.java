package br.com.fiap.autointel.integration;

import br.com.fiap.autointel.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Segurança: JWT, endpoints públicos/protegidos e perfis")
class SegurancaIT extends ApiTestSupport {

    @Autowired
    private JwtProperties jwtProperties;

    private String tokenAssinado(String perfil, Instant emitidoEm, Instant expiraEm) {
        return Jwts.builder()
                .issuer(jwtProperties.emissor())
                .subject(ADMIN_EMAIL)
                .claim("uid", 1)
                .claim("nome", "Administrador")
                .claim("perfil", perfil)
                .issuedAt(Date.from(emitidoEm))
                .expiration(Date.from(expiraEm))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.segredo())))
                .compact();
    }

    // ---------- Endpoints públicos ----------

    @Test
    @DisplayName("Catálogo de atributos, OpenAPI e health são públicos")
    void endpointsPublicos() throws Exception {
        mvc.perform(get("/api/v1/atributos")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    // ---------- Autenticação (401) ----------

    @Test
    @DisplayName("Recurso protegido sem token → 401")
    void semToken() throws Exception {
        mvc.perform(get("/api/v1/veiculos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"))
                .andExpect(jsonPath("$.instance").value("/api/v1/veiculos"));
    }

    @Test
    @DisplayName("Token expirado → 401 com mensagem 'Token expirado'")
    void tokenExpirado() throws Exception {
        Instant agora = Instant.now();
        String expirado = tokenAssinado("ADMIN", agora.minusSeconds(7200), agora.minusSeconds(3600));

        mvc.perform(autenticado(get("/api/v1/veiculos"), expirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", startsWith("Token expirado")));
    }

    @Test
    @DisplayName("Token malformado ou com assinatura inválida → 401")
    void tokenInvalido() throws Exception {
        mvc.perform(autenticado(get("/api/v1/veiculos"), "abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", startsWith("Token inválido")));

        String valido = tokenAnalista();
        String adulterado = valido.substring(0, valido.length() - 4) + "AAAA";
        mvc.perform(autenticado(get("/api/v1/veiculos"), adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token válido gerado com a chave do servidor é aceito (perfil vem da claim)")
    void tokenValidoAceito() throws Exception {
        Instant agora = Instant.now();
        mvc.perform(autenticado(get("/api/v1/usuarios"), tokenAssinado("ADMIN", agora, agora.plusSeconds(300))))
                .andExpect(status().isOk());
    }

    // ---------- Autorização por perfil (403) ----------

    @Test
    @DisplayName("ANALISTA não pode criar veículo, atributo nem listar usuários → 403")
    void analistaSemPermissaoDeEscrita() throws Exception {
        String token = tokenAnalista();
        mvc.perform(autenticado(post("/api/v1/veiculos"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"X\",\"modelo\":\"Y\",\"versao\":\"Z\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        mvc.perform(autenticado(post("/api/v1/atributos"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"XX\",\"nome\":\"X\",\"categoria\":\"OUTROS\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(autenticado(delete("/api/v1/veiculos/1"), token))
                .andExpect(status().isForbidden());
        mvc.perform(autenticado(get("/api/v1/usuarios"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ANALISTA pode ler veículos e realizar consultas")
    void analistaPodeLer() throws Exception {
        String token = tokenAnalista();
        mvc.perform(autenticado(get("/api/v1/veiculos"), token)).andExpect(status().isOk());
        mvc.perform(autenticado(post("/api/v1/consultas"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"Ford\",\"modelo\":\"Ranger\",\"versao\":\"Raptor\",\"atributos\":[\"Motor\"]}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("ADMIN pode listar usuários e promover um analista; novo perfil vale no próximo token")
    void adminGerenciaPerfis() throws Exception {
        String email = unico("promovido") + "@teste.com";
        String criado = mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"P\",\"email\":\"" + email + "\",\"senha\":\"Senha@123\"}"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(criado).get("id").asLong();
        String tokenAntigo = login(email, "Senha@123");

        mvc.perform(autenticado(get("/api/v1/usuarios"), tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)));
        mvc.perform(autenticado(patch("/api/v1/usuarios/" + id + "/perfil"), tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        mvc.perform(autenticado(get("/api/v1/usuarios"), tokenAntigo)).andExpect(status().isForbidden());
        mvc.perform(autenticado(get("/api/v1/usuarios"), login(email, "Senha@123"))).andExpect(status().isOk());
    }
}
