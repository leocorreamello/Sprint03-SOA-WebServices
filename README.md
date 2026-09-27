# Sprint03-SOA-WebServices — AutoIntel

**AutoIntel** é uma API REST de **Inteligência Competitiva Automotiva**. O usuário informa **marca, modelo, versão** e uma **lista livre de equipamentos/atributos técnicos** (com as palavras que quiser: "câmbio", "potência", "vão livre"...) e recebe uma **lista padronizada de especificações**: sempre com o mesmo formato, campos claros e comparáveis, e com a ausência de informação indicada de forma explícita.

## Integrantes

| Nome | RM |
|---|---|
| Leonardo Correa de Mello | RM 555573 |
| Felipe Soares Xavier | RM 556931 |
| Pedro Visconti Guidotte | RM 556630 |
| Herbert de Sousa Vilela | RM 555701 |
| Gabriel Figueira Flora | RM 556476 |

---

## Sumário

1. [Desafio e solução](#1-desafio-e-solução)
2. [Tecnologias](#2-tecnologias)
3. [Como executar](#3-como-executar)
4. [Usando a API (passo a passo)](#4-usando-a-api-passo-a-passo)
5. [Endpoints](#5-endpoints)
6. [Saída padronizada](#6-saída-padronizada)
7. [Validação: Ford Ranger Raptor](#7-validação-ford-ranger-raptor)
8. [Autenticação, autorização e JWT](#8-autenticação-autorização-e-jwt)
9. [Tratamento de erros](#9-tratamento-de-erros)
10. [Testes automatizados](#10-testes-automatizados)
11. [Arquitetura e estrutura do projeto](#11-arquitetura-e-estrutura-do-projeto)
12. [Mapa dos critérios de avaliação](#12-mapa-dos-critérios-de-avaliação)

---

## 1. Desafio e solução

**Desafio 01 — Inteligência Competitiva Automotiva:** criar uma ferramenta que receba dados técnicos da concorrência a partir de uma entrada simples e gere uma lista padronizada de especificações.

| Requisito do desafio | Como a solução atende |
|---|---|
| Usuário define **livremente** a lista de atributos | `POST /api/v1/consultas` aceita qualquer lista de termos. Um **motor de regras** reconhece código, nome, **sinônimos**, variações sem acento ou em caixa alta e até **erros de digitação** ("potencai" → Potência) |
| Entradas: Marca, Modelo, Versão | Campos obrigatórios da consulta, com busca que ignora caixa, acentos e espaços |
| Saída com **formato sempre igual** | Toda linha tem exatamente os mesmos 10 campos, na ordem pedida, para qualquer veículo (existente ou não) |
| Campos **claros, organizados e comparáveis** | Código canônico (`POTENCIA`), nome, categoria, valor bruto, unidade, valor formatado (`397 cv`), fonte e status |
| Informação inexistente **explícita** | `status: NAO_DISPONIVEL`, `valor: null`, `valorFormatado: "Não disponível"`. Termo desconhecido: `ATRIBUTO_NAO_RECONHECIDO` |
| Validação com a **Ranger Raptor** | Base carregada com 62 especificações da Raptor e teste automatizado dedicado (`RangerRaptorValidacaoIT`) |

**Abordagem técnica:** um motor de regras determinístico (normalização de texto, catálogo de 72 atributos com sinônimos e similaridade Damerau-Levenshtein). Assim o mesmo pedido sempre gera a mesma resposta, todo valor tem fonte rastreável e nada é inventado. Também é possível incluir novos atributos, sinônimos e veículos pela própria API, sem mexer em código.

## 2. Tecnologias

- **Java 21** · **Spring Boot 4.0.8** (Web MVC, Data JPA, Validation, Security, Actuator)
- **JWT** com [jjwt 0.13](https://github.com/jwtk/jjwt) (HMAC-SHA256)
- **H2** (banco em memória) · **Hibernate 7**
- **springdoc-openapi 3** (OpenAPI 3.1 + Swagger UI)
- **JUnit 5 + MockMvc + AssertJ** · **JaCoCo** (cobertura)
- **Maven Wrapper** (não é preciso ter o Maven instalado)

## 3. Como executar

### Pré-requisitos
- **JDK 21 ou superior** (testado com JDK 25). Para verificar: `java -version`

### Pelo terminal

```bash
# macOS / Linux
JWT_SECRET="$(openssl rand -base64 32)" ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
$env:JWT_SECRET=[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:SPRING_PROFILES_ACTIVE='dev'
.\mvnw.cmd spring-boot:run
```

Outra opção é gerar o JAR e executá-lo:

```bash
./mvnw clean package
JWT_SECRET="$(openssl rand -base64 32)" java -jar target/autointel-1.0.0.jar
```

### Pelo IntelliJ IDEA
1. Abra a pasta do projeto. Se o IntelliJ oferecer *"Load Maven Project"*, aceite (ou clique com o botão direito em `pom.xml` → *Add as Maven Project*).
2. Configure o SDK do projeto como JDK 21+ (*File → Project Structure → SDK*).
3. Execute a classe `br.com.fiap.autointel.AutointelApplication`.

### Endereços

| O quê | URL |
|---|---|
| **Swagger UI** (documentação interativa) | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Health check | http://localhost:8080/actuator/health |
| Console H2 | http://localhost:8080/h2-console (somente no perfil `dev`) |
| Métricas HTTP | http://localhost:8080/actuator/metrics (exige JWT ADMIN) |

### Usuários de demonstração (somente no perfil `dev` ou nos testes)

| Perfil | E-mail | Senha |
|---|---|---|
| ADMIN | `admin@autointel.com` | `Admin@123` |
| ANALISTA | `analista@autointel.com` | `Analista@123` |

### Configuração de segurança

| Variável | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | sem padrão | Obrigatória; chave HMAC em Base64 com mínimo de 256 bits. Gerar valor diferente por ambiente. |
| `JWT_EXPIRACAO` | `1h` | Validade do token (ex.: `30m`, `2h`) |
| `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` | vazios | Opcionais; criam o primeiro ADMIN se ainda não existir. Informar ambos e senha com pelo menos 12 caracteres. |

O perfil padrão desativa o console H2 e as contas de demonstração. O banco H2 continua em memória: seus dados são perdidos ao reiniciar. Para persistência, backup e recuperação reais, migrar para um banco persistente. O limitador de login mantém estado em memória por instância (5 falhas por endereço remoto e conta, ou 30 falhas totais por endereço, em 15 minutos); em produção com múltiplas réplicas, substituir por armazenamento compartilhado ou limite no gateway. Atrás de proxy, configurar o endereço remoto confiável antes de usar este controle como proteção principal.

O pipeline de segurança está em [`.github/workflows/devsecops.yml`](.github/workflows/devsecops.yml); o `Dockerfile` executa com UID sem privilégios. Logs do console usam JSON/ECS. O endpoint `/actuator/metrics` exige perfil ADMIN.

## 4. Usando a API (passo a passo)

**Pelo Swagger:** abra o Swagger UI, execute `POST /api/v1/auth/login`, copie o `token`, clique em **Authorize**, cole o token e use os demais endpoints.

**Pelo IntelliJ/VS Code:** o arquivo [`requests.http`](requests.http) tem todas as chamadas prontas, com os tokens guardados automaticamente.

**Por curl:**

```bash
# 1) Login (público) → guarda o token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"analista@autointel.com","senha":"Analista@123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2) Consulta com lista livre de atributos
curl -s -X POST http://localhost:8080/api/v1/consultas \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"marca":"Ford","modelo":"Ranger","versao":"Raptor",
       "atributos":["Preço","Potência","torque","Câmbio","Pneus","Teto solar","Cafeteira"]}'

# 3) Exportar o resultado em CSV
curl -s http://localhost:8080/api/v1/consultas/1 -H "Authorization: Bearer $TOKEN" -H 'Accept: text/csv'
```

## 5. Endpoints

Base: `/api/v1` · 🔓 público · 🔐 exige JWT

| Método | Recurso | Acesso | Sucesso | Erros possíveis | Descrição |
|---|---|---|---|---|---|
| POST | `/auth/login` | 🔓 | 200 | 400, 401 | Autentica e devolve o JWT |
| GET | `/auth/me` | 🔐 qualquer perfil | 200 | 401 | Dados do usuário lidos do token |
| POST | `/usuarios` | 🔓 | 201 + `Location` | 400, 409 | Cadastro (sempre ANALISTA) |
| GET | `/usuarios` · `/usuarios/{id}` | 🔐 ADMIN | 200 | 401, 403, 404 | Lista/detalha usuários |
| PATCH | `/usuarios/{id}/perfil` | 🔐 ADMIN | 200 | 400, 403, 404 | Altera o perfil |
| **POST** | **`/consultas`** | 🔐 ANALISTA, ADMIN | **201** + `Location` | 400, 401 | **Gera a lista padronizada** |
| GET | `/consultas` | 🔐 ANALISTA, ADMIN | 200 | 401 | Histórico paginado (analista: só o próprio) |
| GET | `/consultas/{id}` | 🔐 dono ou ADMIN | 200 (JSON ou CSV) | 401, 403, 404 | Resultado de uma consulta |
| DELETE | `/consultas/{id}` | 🔐 dono ou ADMIN | 204 | 401, 403, 404 | Remove do histórico |
| GET | `/veiculos?marca=&modelo=` | 🔐 ANALISTA, ADMIN | 200 | 401 | Lista veículos da base |
| GET | `/veiculos/{id}` | 🔐 ANALISTA, ADMIN | 200 | 401, 404 | Detalha veículo |
| POST | `/veiculos` | 🔐 ADMIN | 201 + `Location` | 400, 403, 409 | Cadastra veículo |
| PUT | `/veiculos/{id}` | 🔐 ADMIN | 200 | 400, 403, 404, 409 | Atualiza veículo |
| DELETE | `/veiculos/{id}` | 🔐 ADMIN | 204 | 403, 404 | Remove veículo |
| GET | `/veiculos/{id}/especificacoes` | 🔐 ANALISTA, ADMIN | 200 | 401, 404 | Todas as especificações do veículo |
| GET | `/veiculos/{id}/especificacoes/{codigo}` | 🔐 ANALISTA, ADMIN | 200 | 401, 404 | Um valor específico |
| PUT | `/veiculos/{id}/especificacoes/{codigo}` | 🔐 ADMIN | 201 (criou) / 200 (substituiu) | 400, 403, 404 | Define valor (idempotente) |
| DELETE | `/veiculos/{id}/especificacoes/{codigo}` | 🔐 ADMIN | 204 | 403, 404 | Remove valor |
| GET | `/atributos?categoria=` | 🔓 | 200 | 400 | Catálogo de atributos e sinônimos |
| GET | `/atributos/{codigo}` | 🔓 | 200 | 404 | Detalha atributo |
| POST | `/atributos` | 🔐 ADMIN | 201 + `Location` | 400, 403, 409 | Novo atributo |
| PUT | `/atributos/{codigo}` | 🔐 ADMIN | 200 | 400, 403, 404 | Atualiza atributo/sinônimos |
| DELETE | `/atributos/{codigo}` | 🔐 ADMIN | 204 | 403, 404, 409 | Remove atributo sem uso |

## 6. Saída padronizada

Requisição:

```json
{
  "marca": "Ford", "modelo": "Ranger", "versao": "Raptor",
  "atributos": ["Potência", "Câmbio", "Teto solar", "Cafeteira"]
}
```

Resposta `201 Created` (`Location: /api/v1/consultas/1`), com a fonte resumida aqui para facilitar a leitura:

```json
{
  "id": 1,
  "veiculo": { "encontrado": true, "veiculoId": 1, "marca": "Ford", "modelo": "Ranger", "versao": "Raptor", "anoModelo": 2025 },
  "realizadaEm": "2026-09-26T22:09:52.085Z",
  "solicitante": "analista@autointel.com",
  "resumo": { "totalSolicitados": 4, "disponiveis": 2, "naoDisponiveis": 1, "naoReconhecidos": 1 },
  "especificacoes": [
    { "ordem": 1, "termoSolicitado": "Potência", "codigo": "POTENCIA", "atributo": "Potência máxima", "categoria": "MOTOR",
      "valor": "397", "unidade": "cv", "valorFormatado": "397 cv", "fonte": "Ficha técnica Ford...", "status": "DISPONIVEL" },
    { "ordem": 2, "termoSolicitado": "Câmbio", "codigo": "TRANSMISSAO", "atributo": "Transmissão", "categoria": "TRANSMISSAO",
      "valor": "Automática de 10 velocidades", "unidade": null, "valorFormatado": "Automática de 10 velocidades", "fonte": "Ficha técnica Ford...", "status": "DISPONIVEL" },
    { "ordem": 3, "termoSolicitado": "Teto solar", "codigo": "TETO_SOLAR", "atributo": "Teto solar", "categoria": "CONFORTO",
      "valor": null, "unidade": null, "valorFormatado": "Não disponível", "fonte": null, "status": "NAO_DISPONIVEL" },
    { "ordem": 4, "termoSolicitado": "Cafeteira", "codigo": null, "atributo": null, "categoria": null,
      "valor": null, "unidade": null, "valorFormatado": "Atributo não reconhecido", "fonte": null, "status": "ATRIBUTO_NAO_RECONHECIDO" }
  ]
}
```

| Campo | Significado |
|---|---|
| `ordem` | Posição na lista enviada pelo usuário |
| `termoSolicitado` | Termo exatamente como o usuário digitou |
| `codigo` / `atributo` / `categoria` | Identificação canônica: é o que permite comparar veículos diferentes |
| `valor` / `unidade` | Valor bruto e unidade separados (úteis para planilhas e comparação) |
| `valorFormatado` | Texto pronto para exibição (`R$ 424.990`, `272 mm`, `32°`, `Não disponível`) |
| `fonte` | Origem do dado (rastreabilidade) |
| `status` | `DISPONIVEL` · `NAO_DISPONIVEL` · `ATRIBUTO_NAO_RECONHECIDO` |

A mesma consulta pode ser exportada em **CSV** (separador `;`, abre direto no Excel) via `GET /api/v1/consultas/{id}` com `Accept: text/csv`.

## 7. Validação: Ford Ranger Raptor

A base inicial ([`src/main/resources/seed/veiculos.json`](src/main/resources/seed/veiculos.json)) contém **62 especificações** da Ford Ranger Raptor: preço, motor, cilindrada, potência, torque, câmbio, tração, reduzida, bloqueios, modos de condução, suspensões, amortecedores FOX, rodas, pneus, dimensões, ângulos off-road, capacidade de imersão, tanque, reboque, airbags, ADAS, multimídia, painel, som, faróis etc.

- **Exemplo completo:** [requisição](docs/exemplos/consulta-ranger-raptor.json) com 64 atributos → [resposta](docs/exemplos/resposta-ranger-raptor.json) com 62 `DISPONIVEL` e 2 `NAO_DISPONIVEL` (teto solar e consumo, que não constam da ficha).
- **Teste automatizado:** [`RangerRaptorValidacaoIT`](src/test/java/br/com/fiap/autointel/integration/RangerRaptorValidacaoIT.java) usa termos livres ("Câmbio", "vão livre do solo", "Ângulo de ataque", "Som"...) e confere o valor formatado de cada item, a ordem, o status e a consistência do formato entre veículos.

> ⚠️ **Conferência com o slide:** os valores foram montados a partir da ficha técnica pública da Ranger Raptor. **Antes da entrega, compare-os com o slide do desafio.** Se algum valor for diferente (preço, por exemplo), basta editar `seed/veiculos.json` e a tabela `ESPERADO` em `RangerRaptorValidacaoIT`. Não é preciso alterar nenhum código. Outra opção é ajustar em tempo de execução com `PUT /api/v1/veiculos/1/especificacoes/{codigo}` (ADMIN).

Um segundo veículo (Toyota Hilux GR-Sport, com dados parciais) demonstra que o formato é o mesmo para qualquer veículo e que os dados faltantes aparecem como `NAO_DISPONIVEL`.

## 8. Autenticação, autorização e JWT

- **Login:** `POST /api/v1/auth/login` → `{ "tipo": "Bearer", "token": "...", "expiraEmSegundos": 3600, "expiraEm": "...", "usuario": {...} }`
- **Uso:** header `Authorization: Bearer <token>` em todos os endpoints protegidos.
- **Token:** HS256 com as claims `sub` (e-mail), `uid`, `nome`, `perfil`, `iss` (`autointel-api`), `iat`, `exp` e `jti`.
- **Validação a cada requisição:** assinatura, emissor e expiração. Token expirado → `401 "Token expirado..."`; token adulterado ou malformado → `401 "Token inválido..."`.
- **Stateless:** sem sessão no servidor. O perfil usado na autorização vem da claim `perfil`.
- **Perfis:** `ADMIN` (mantém catálogo, veículos e usuários; vê todas as consultas) e `ANALISTA` (faz consultas e vê apenas o próprio histórico). Endpoints públicos: login, cadastro, catálogo de atributos, Swagger e health.
- **Senhas** armazenadas com BCrypt.

A matriz completa de permissões e o diagrama de sequência da autenticação estão em [docs/ARQUITETURA.md](docs/ARQUITETURA.md#3-fluxo-de-autenticação-jwt).

## 9. Tratamento de erros

Todos os erros, inclusive 401/403 do Spring Security e 404/405/415 do Spring MVC, seguem o padrão **Problem Details (RFC 9457)**, com `Content-Type: application/problem+json`:

```json
{
  "type": "https://autointel.fiap.com.br/erros/validacao",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/api/v1/consultas",
  "codigo": "VALIDACAO",
  "timestamp": "2026-09-26T22:09:52.113Z",
  "erros": [ { "campo": "marca", "mensagem": "não deve estar em branco" } ]
}
```

| `codigo` | Status | Quando |
|---|---|---|
| `VALIDACAO` / `JSON_INVALIDO` / `PARAMETRO_INVALIDO` | 400 | Campos inválidos, JSON malformado, parâmetro com tipo errado |
| `NAO_AUTENTICADO` | 401 | Token ausente, inválido, expirado ou login inválido |
| `LIMITE_LOGIN` | 429 | Sexta falha da mesma origem/conta ou 31ª falha total da origem em 15 minutos |
| `ACESSO_NEGADO` | 403 | Perfil sem permissão ou consulta de outro usuário |
| `RECURSO_NAO_ENCONTRADO` | 404 | Recurso ou rota inexistente |
| `METODO_NAO_SUPORTADO` | 405 | Método HTTP não suportado no recurso |
| `CONFLITO` | 409 | Duplicidade (e-mail, veículo, código de atributo) ou atributo em uso |
| `TIPO_DE_MIDIA_NAO_SUPORTADO` | 415 | `Content-Type` não suportado |
| `ERRO_INTERNO` | 500 | Erro inesperado (sem stack trace na resposta) |

## 10. Testes automatizados

```bash
./mvnw test      # executa os 84 testes (unitários + integração da API)
./mvnw verify    # testes + relatório de cobertura JaCoCo + relatório HTML dos testes
python3 scripts/gerar-evidencias.py   # (opcional) atualiza docs/evidencias/RESULTADO_TESTES.md
```

| Suíte | Tipo | O que cobre |
|---|---|---|
| `RangerRaptorValidacaoIT` | Integração | Validação do desafio: todas as especificações, formato idêntico entre veículos, dados ausentes explícitos |
| `ConsultaIT` | Integração | 201 + Location, 400, 401, 403 (consulta de outro usuário), 404, CSV, paginação, DELETE 204 |
| `SegurancaIT` | Integração | Endpoints públicos, sem token, token expirado, token adulterado, 403 por perfil, promoção de perfil |
| `AutenticacaoIT` | Integração | Login (200/401/400), cadastro (201/409/400), `/auth/me` |
| `VeiculoEspecificacaoIT` | Integração | CRUD de veículos e especificações (201/200/204/404/409/403), PUT idempotente |
| `AtributoIT` | Integração | Catálogo público, CRUD do ADMIN, 409 atributo  em uso |
| `TratamentoErrosIT` | Integração | Formato padrão de erro, 400/404/405/415 |
| `JwtServiceTest` | Unitário | Geração, claims, expiração, adulteração, outra chave, outro emissor |
| `ReconhecedorAtributosTest` | Unitário | Sinônimos, acentos, erros de digitação, termos desconhecidos, catálogo sem ambiguidades |
| `NormalizadorTextoTest` | Unitário | Normalização e similaridade de textos |

**Evidências da execução:** a suíte atual contém **84 testes, 0 falhas** (inclui rate limit, proteção contra spray de contas, acesso ADMIN ao Actuator e exposição Prometheus restrita ao perfil local `monitor`). [docs/evidencias/RESULTADO_TESTES.md](docs/evidencias/RESULTADO_TESTES.md) contém o registro anterior de 80 testes; após o `verify`, os relatórios atualizados ficam em `target/reports/surefire.html` e `target/site/jacoco/index.html`.

### Monitoramento da Sprint 3

O diretório [monitoring](monitoring/README.md) contém uma stack local reproduzível com a API, Prometheus e Grafana. O perfil `monitor` habilita `/actuator/prometheus`; fora dele, o endpoint permanece indisponível. O dashboard provisionado mostra disponibilidade, tráfego HTTP, respostas 401/403/429/5xx e latência p95. A stack publica portas somente em `127.0.0.1` e não representa um deploy de produção.

## 11. Arquitetura e estrutura do projeto

O documento completo está em **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)** e inclui os diagramas de contexto e de componentes, os fluxos de autenticação e de consulta, o modelo de dados e as decisões técnicas.

```
src/main/java/br/com/fiap/autointel
├── config/          SecurityConfig (regras por perfil), OpenApiConfig, DataSeeder
├── security/        JwtService, JwtAuthenticationFilter, EntryPoint 401, AccessDenied 403
├── web/controller/  Recursos REST (Auth, Usuario, Consulta, Veiculo, Especificacao, Atributo)
├── web/dto/         Contratos de entrada/saída (records)
├── service/         Regras de negócio — ConsultaService, ReconhecedorAtributos, ...
├── domain/          Entidades JPA, repositórios e NormalizadorTexto
└── exception/       GlobalExceptionHandler (Problem Details) e exceções de negócio
src/main/resources
├── application.properties
└── seed/            atributos.json (catálogo + sinônimos) e veiculos.json (Ranger Raptor, Hilux)
docs/                ARQUITETURA.md, exemplos/, evidencias/
requests.http        Coleção de requisições prontas
```

## 12. Mapa dos critérios de avaliação

| Critério (peso) | Onde está |
|---|---|
| Arquitetura da Solução (20%) | [docs/ARQUITETURA.md](docs/ARQUITETURA.md): diagramas de componentes, responsabilidades por camada, fluxos de comunicação e de autenticação |
| Maturidade REST Nível 2 (20%) | Recursos por substantivo, verbos GET/POST/PUT/PATCH/DELETE, status 200/201/204/400/401/403/404/405/409/415, `Location`, PUT idempotente, negociação JSON/CSV ([seção 6 da arquitetura](docs/ARQUITETURA.md#6-maturidade-rest--nível-2-richardson)) |
| Autenticação e Autorização (20%) | `SecurityConfig` + `@PreAuthorize`; endpoints públicos e protegidos; perfis ADMIN/ANALISTA; controle por dono da consulta |
| Testes Automatizados (15%) | 80 testes (sucesso, erro e acesso não autorizado) · [evidências](docs/evidencias/RESULTADO_TESTES.md) |
| JWT (15%) | `JwtService` (geração/validação HS256, emissor, expiração), `JwtAuthenticationFilter`, claims usadas na autorização e no `/auth/me` |
| Documentação e Erros (10%) | Swagger/OpenAPI em `/swagger-ui.html`, Problem Details padronizado, este README |
