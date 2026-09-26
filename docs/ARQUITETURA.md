# Arquitetura da Solução — AutoIntel

> API REST de **Inteligência Competitiva Automotiva**: recebe *marca, modelo, versão* e uma **lista livre de atributos técnicos** e devolve uma **lista padronizada de especificações**, sempre no mesmo formato, com ausência de dados explícita.

## 1. Visão geral (contexto)

```mermaid
flowchart LR
    analista["👤 Analista de mercado<br/>(perfil ANALISTA)"]
    admin["👤 Administrador<br/>(perfil ADMIN)"]
    cliente["Cliente HTTP<br/>Swagger UI · Postman · Front-end"]

    subgraph api["AutoIntel API (Spring Boot 4 · Java 21)"]
        direction TB
        sec["Camada de Segurança<br/>JWT + perfis"]
        rest["Camada Web (REST nível 2)"]
        svc["Camada de Serviço<br/>regras de negócio"]
        repo["Camada de Persistência<br/>Spring Data JPA"]
        sec --> rest --> svc --> repo
    end

    db[("Banco H2<br/>(em memória)")]
    seed[/"Carga inicial<br/>seed/*.json"/]

    analista --> cliente
    admin --> cliente
    cliente -- "HTTPS + JSON<br/>Authorization: Bearer &lt;JWT&gt;" --> api
    repo --> db
    seed -. "na inicialização" .-> db
```

## 2. Componentes e responsabilidades

```mermaid
flowchart TB
    subgraph web["web.controller — Recursos REST"]
        AuthC["AuthController<br/>/api/v1/auth"]
        UsuC["UsuarioController<br/>/api/v1/usuarios"]
        ConsC["ConsultaController<br/>/api/v1/consultas"]
        VeiC["VeiculoController<br/>/api/v1/veiculos"]
        EspC["EspecificacaoController<br/>/api/v1/veiculos/{id}/especificacoes"]
        AtrC["AtributoController<br/>/api/v1/atributos"]
    end

    subgraph security["security — Autenticação/Autorização"]
        Filter["JwtAuthenticationFilter"]
        JwtS["JwtService<br/>(gera/valida HS256)"]
        EP["RestAuthenticationEntryPoint (401)"]
        ADH["RestAccessDeniedHandler (403)"]
        UDS["UsuarioDetailsService (BCrypt)"]
    end

    subgraph service["service — Regras de negócio"]
        AuthS["AuthService"]
        UsuS["UsuarioService"]
        ConsS["ConsultaService<br/>⭐ gera a lista padronizada"]
        Rec["ReconhecedorAtributos<br/>⭐ motor de regras (termo livre → atributo)"]
        Fmt["FormatadorValor"]
        VeiS["VeiculoService"]
        AtrS["AtributoService"]
    end

    subgraph domain["domain — Modelo e persistência"]
        Ent["Entidades JPA<br/>Usuario · Atributo · Veiculo · Especificacao · Consulta · ConsultaItem"]
        Repo["Repositórios Spring Data"]
        Norm["NormalizadorTexto"]
    end

    Handler["exception.GlobalExceptionHandler<br/>Problem Details (RFC 9457)"]
    DB[("H2")]

    Filter --> JwtS
    AuthC --> AuthS --> UDS
    AuthS --> JwtS
    UsuC --> UsuS
    ConsC --> ConsS --> Rec --> Norm
    ConsS --> Fmt
    VeiC --> VeiS
    EspC --> VeiS
    AtrC --> AtrS
    VeiS --> AtrS
    AuthS & UsuS & ConsS & VeiS & AtrS --> Repo --> DB
    Repo --- Ent
    EP -. delega .-> Handler
    ADH -. delega .-> Handler
    web -. exceções .-> Handler
```

| Camada / Pacote | Responsabilidade | Não faz |
|---|---|---|
| `web.controller` | Mapear recursos e métodos HTTP, validar entrada (`@Valid`), escolher status code e headers (`Location`), converter DTOs | Regra de negócio, acesso a banco |
| `web.dto` | Contratos de entrada/saída (records imutáveis) — isolam a API das entidades | Lógica |
| `security` | Emitir e validar JWT, montar o usuário autenticado a partir das claims, responder 401/403 | Regras de negócio |
| `config` | Regras de acesso por rota/perfil (`SecurityConfig`), OpenAPI, carga inicial | — |
| `service` | Casos de uso: consulta padronizada, reconhecimento de atributos, CRUDs, controle de dono da consulta | Conhecer HTTP |
| `domain` | Entidades, invariantes (ex.: unicidade marca/modelo/versão normalizada) e repositórios | Conhecer HTTP ou DTOs |
| `exception` | Traduzir exceções em respostas de erro padronizadas | — |

## 3. Fluxo de autenticação (JWT)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário
    participant F as JwtAuthenticationFilter
    participant AC as AuthController
    participant AM as AuthenticationManager<br/>(DaoAuthenticationProvider + BCrypt)
    participant J as JwtService
    participant Z as AuthorizationFilter<br/>(regras por perfil)
    participant C as Controller protegido
    participant H as GlobalExceptionHandler

    Note over U,J: 1) Obtenção do token (endpoint público)
    U->>AC: POST /api/v1/auth/login {email, senha}
    AC->>AM: authenticate(email, senha)
    alt credenciais inválidas
        AM-->>H: BadCredentialsException
        H-->>U: 401 Problem Details
    else credenciais válidas
        AM-->>AC: ok
        AC->>J: gerar(usuario)
        J-->>AC: JWT HS256 {sub, uid, nome, perfil, iss, iat, exp, jti}
        AC-->>U: 200 {tipo: Bearer, token, expiraEmSegundos, expiraEm, usuario}
    end

    Note over U,C: 2) Acesso a recurso protegido (stateless — nenhuma sessão no servidor)
    U->>F: GET /api/v1/veiculos<br/>Authorization: Bearer <JWT>
    F->>J: validar(token) — assinatura, emissor, expiração
    alt token ausente / inválido / expirado
        F->>Z: segue sem autenticação
        Z-->>H: EntryPoint → 401 ("Token expirado" / "Token inválido")
        H-->>U: 401 Problem Details + WWW-Authenticate: Bearer
    else token válido
        J-->>F: UsuarioAutenticado (a partir das claims)
        F->>Z: SecurityContext com ROLE_<perfil>
        alt perfil sem permissão
            Z-->>H: AccessDeniedHandler → 403
            H-->>U: 403 Problem Details
        else perfil autorizado
            Z->>C: requisição autorizada
            C-->>U: 2xx
        end
    end
```

**Decisões de segurança**

- **Stateless:** o servidor não guarda sessão; o JWT carrega `uid`, `nome` e `perfil`, evitando ida ao banco a cada requisição.
- **Expiração** configurável (`app.jwt.expiracao`, padrão 1h) e validada com relógio injetável (testável).
- **Emissor** (`iss`) validado: tokens de outros sistemas, mesmo com a mesma chave, são rejeitados.
- **Senhas** com BCrypt; mensagem de login inválido não revela se o e-mail existe.
- **Defesa em profundidade:** regras por rota no `SecurityConfig` **e** `@PreAuthorize` nos métodos de escrita.
- **Controle por dono:** um ANALISTA só lê/remove as próprias consultas (403 caso contrário); ADMIN vê todas.
- **Promoção de perfil** apenas por ADMIN (`PATCH /usuarios/{id}/perfil`); o novo perfil vale a partir do próximo token.

### Perfis e permissões

| Recurso | Público | ANALISTA | ADMIN |
|---|:---:|:---:|:---:|
| `POST /auth/login`, `POST /usuarios` (cadastro) | ✅ | ✅ | ✅ |
| `GET /atributos/**` (catálogo) | ✅ | ✅ | ✅ |
| Swagger UI, `/v3/api-docs`, `/actuator/health` | ✅ | ✅ | ✅ |
| `GET /auth/me` | ❌ | ✅ | ✅ |
| `POST/GET/DELETE /consultas/**` | ❌ | ✅ (só as próprias) | ✅ (todas) |
| `GET /veiculos/**` (inclui especificações) | ❌ | ✅ | ✅ |
| `POST/PUT/DELETE /veiculos/**`, `/atributos/**` | ❌ | ❌ | ✅ |
| `GET /usuarios/**`, `PATCH /usuarios/{id}/perfil` | ❌ | ❌ | ✅ |

## 4. Fluxo da consulta padronizada (núcleo do desafio)

```mermaid
sequenceDiagram
    autonumber
    actor A as Analista
    participant C as ConsultaController
    participant S as ConsultaService
    participant V as VeiculoRepository
    participant R as ReconhecedorAtributos
    participant DB as H2

    A->>C: POST /api/v1/consultas<br/>{marca, modelo, versao, atributos:[termos livres]}
    C->>S: realizar(request, usuario do token)
    S->>V: findByChaveBusca("ford|ranger|raptor")
    Note right of S: chave normalizada: sem acento,<br/>minúsculas, sem pontuação
    S->>R: indexar(catálogo de atributos)
    loop para cada termo (na ordem enviada, sem duplicados)
        S->>R: reconhecer("Câmbio")
        R-->>S: TRANSMISSAO (exato por código/nome/sinônimo<br/>ou aproximado — Damerau-Levenshtein ≥ 0,85)
        alt termo não reconhecido
            S->>S: linha ATRIBUTO_NAO_RECONHECIDO
        else veículo sem valor para o atributo
            S->>S: linha NAO_DISPONIVEL (valor = null)
        else valor encontrado
            S->>S: linha DISPONIVEL (valor + unidade + fonte)
        end
    end
    S->>DB: salva Consulta + itens ("fotografia" do resultado)
    C-->>A: 201 Created · Location: /api/v1/consultas/{id}<br/>lista padronizada
```

**Por que um motor de regras (e não IA generativa)?** O desafio permite qualquer abordagem. Optamos por regras determinísticas (normalização + sinônimos + tolerância a erros de digitação) porque:
1. **Consistência:** o mesmo pedido sempre gera a mesma saída — requisito central ("formato sempre o mesmo").
2. **Rastreabilidade:** cada valor tem `fonte`; nada é "inventado". Quando não há dado, a saída diz explicitamente `NAO_DISPONIVEL`.
3. **Extensível sem código:** novos atributos/sinônimos entram pela API (`POST /atributos`) ou pelo `seed/atributos.json`.

A arquitetura em camadas permite, no futuro, plugar um provedor de IA/scraping como fonte adicional de especificações dentro do `service`, sem alterar os contratos REST.

## 5. Modelo de dados

```mermaid
erDiagram
    USUARIO {
        bigint id PK
        string nome
        string email UK
        string senha "BCrypt"
        enum perfil "ADMIN | ANALISTA"
    }
    ATRIBUTO {
        bigint id PK
        string codigo UK "ex.: POTENCIA"
        string nome
        enum categoria
        string unidade "ex.: cv"
    }
    ATRIBUTO_SINONIMOS {
        bigint atributo_id FK
        string sinonimo
    }
    VEICULO {
        bigint id PK
        string marca
        string modelo
        string versao
        int anoModelo
        string chaveBusca UK "marca|modelo|versao normalizados"
    }
    ESPECIFICACAO {
        bigint id PK
        bigint veiculo_id FK
        bigint atributo_id FK
        string valor
        string fonte
    }
    CONSULTA {
        bigint id PK
        string marca
        string modelo
        string versao
        bigint veiculo_id FK "nulo se não encontrado"
        string solicitante "e-mail do token"
        timestamp realizadaEm
    }
    CONSULTA_ITEM {
        bigint id PK
        bigint consulta_id FK
        int ordem
        string termoSolicitado
        string codigoAtributo
        string valor
        enum status "DISPONIVEL | NAO_DISPONIVEL | ATRIBUTO_NAO_RECONHECIDO"
    }
    ATRIBUTO ||--o{ ATRIBUTO_SINONIMOS : possui
    VEICULO ||--o{ ESPECIFICACAO : possui
    ATRIBUTO ||--o{ ESPECIFICACAO : "é valorado em"
    VEICULO |o--o{ CONSULTA : "é consultado em"
    CONSULTA ||--|{ CONSULTA_ITEM : contem
```

## 6. Maturidade REST — Nível 2 (Richardson)

| Critério | Como foi atendido |
|---|---|
| **Recursos** identificados por URI (substantivos) | `/usuarios`, `/atributos/{codigo}`, `/veiculos/{id}`, `/veiculos/{id}/especificacoes/{codigoAtributo}` (sub-recurso), `/consultas/{id}` |
| **Verbos HTTP** com semântica correta | `GET` leitura (seguro/idempotente) · `POST` criação · `PUT` substituição idempotente · `PATCH` alteração parcial (perfil) · `DELETE` remoção |
| **Status codes** coerentes | `200` leitura/atualização · `201` + `Location` criação · `204` remoção · `400` validação · `401` token ausente/inválido/expirado · `403` sem permissão · `404` inexistente · `405` método · `409` conflito · `415` mídia |
| **PUT idempotente** criando ou substituindo | `PUT /veiculos/{id}/especificacoes/POTENCIA` → `201` na 1ª vez, `200` nas seguintes |
| **Negociação de conteúdo** | `GET /consultas/{id}` com `Accept: application/json` ou `Accept: text/csv` |
| **Paginação** | `GET /consultas?page=0&size=20&sort=realizadaEm,desc` |
| **Versionamento** | prefixo `/api/v1` |

## 7. Tratamento de erros

Todas as respostas de erro — inclusive as geradas pelo Spring Security (401/403) e pelo Spring MVC (404/405/415) — seguem o **Problem Details (RFC 9457)**, com `Content-Type: application/problem+json`:

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

O `RestAuthenticationEntryPoint` e o `RestAccessDeniedHandler` delegam ao `GlobalExceptionHandler` via `HandlerExceptionResolver`, garantindo **um único ponto** de formatação de erros.
