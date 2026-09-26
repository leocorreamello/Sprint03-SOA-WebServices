package br.com.fiap.autointel.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AutoIntel API - Inteligência Competitiva Automotiva",
                version = "1.0.0",
                description = """
                        Recebe marca, modelo, versão e uma lista **livre** de equipamentos/atributos técnicos e devolve \
                        uma lista **padronizada** de especificações — sempre no mesmo formato, com ausência de \
                        informação explícita (`status = NAO_DISPONIVEL`).

                        **Como autenticar:** faça `POST /api/v1/auth/login`, copie o `token` e clique em **Authorize**.

                        Usuários de demonstração: `admin@autointel.com / Admin@123` (ADMIN) e \
                        `analista@autointel.com / Analista@123` (ANALISTA).""",
                contact = @Contact(name = "Equipe Sprint 3 - FIAP")),
        tags = {
                @Tag(name = "Autenticação", description = "Login, cadastro e dados do token (público / autenticado)"),
                @Tag(name = "Consultas", description = "Pesquisa padronizada de especificações (ADMIN, ANALISTA)"),
                @Tag(name = "Veículos", description = "Base de veículos da concorrência (leitura: ADMIN/ANALISTA; escrita: ADMIN)"),
                @Tag(name = "Especificações", description = "Valores técnicos de cada veículo (leitura: ADMIN/ANALISTA; escrita: ADMIN)"),
                @Tag(name = "Atributos", description = "Catálogo canônico de atributos e sinônimos (leitura: pública; escrita: ADMIN)")
        })
@SecurityScheme(name = OpenApiConfig.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";
}
