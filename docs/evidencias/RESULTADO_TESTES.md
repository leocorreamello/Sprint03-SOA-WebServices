# Evidência de execução dos testes automatizados

- **Data da execução:** 26/09/2026 19:15
- **Ambiente:** Darwin 27.0.0 · openjdk version "25.0.4" 2026-07-21
- **Comando:** `./mvnw verify`

## Resumo

| Total | Sucesso | Falhas | Ignorados |
|---:|---:|---:|---:|
| 80 | 80 | 0 | 0 |

## Cobertura de código (JaCoCo)

| Métrica | Cobertura |
|---|---:|
| Instruções | 95.1% (3761/3956) |
| Linhas | 94.6% (689/728) |
| Branches | 83.7% (118/141) |
| Métodos | 92.6% (251/271) |

Relatório completo: `target/site/jacoco/index.html` · Relatório HTML dos testes: `target/reports/surefire.html`

## Resultado por suíte

| Suíte | Classe | Testes | Falhas | Tempo (s) |
|---|---|---:|---:|---:|
| Recurso /atributos (catálogo) | `AtributoIT` | 6 | 0 | 0.35 |
| Autenticação e cadastro de usuários | `AutenticacaoIT` | 10 | 0 | 0.30 |
| Recurso /consultas | `ConsultaIT` | 8 | 0 | 0.98 |
| Validação do desafio: Ford Ranger Raptor | `RangerRaptorValidacaoIT` | 4 | 0 | 4.07 |
| Segurança: JWT, endpoints públicos/protegidos e perfis | `SegurancaIT` | 8 | 0 | 0.70 |
| Padronização das respostas de erro (RFC 9457) | `TratamentoErrosIT` | 5 | 0 | 0.30 |
| Recursos /veiculos e /veiculos/{id}/especificacoes | `VeiculoEspecificacaoIT` | 7 | 0 | 0.72 |
| JwtService (geração e validação de JWT) | `JwtServiceTest` | 6 | 0 | 0.22 |
| NormalizadorTexto | `NormalizadorTextoTest` | 9 | 0 | 0.11 |
| ReconhecedorAtributos (motor de regras) | `ReconhecedorAtributosTest` | 17 | 0 | 0.16 |

## Detalhamento dos cenários

### Recurso /atributos (catálogo)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| POST sem token → 401 (escrita no catálogo não é pública) | ✅ OK | 0.009 |
| DELETE de atributo com especificações cadastradas → 409 | ✅ OK | 0.077 |
| GET público lista o catálogo e permite filtrar por categoria | ✅ OK | 0.034 |
| GET /{codigo} público → 200; inexistente → 404; categoria inválida → 400 | ✅ OK | 0.008 |
| ADMIN cria (201), atualiza (200) e remove (204) atributo; novo sinônimo já é reconhecido | ✅ OK | 0.148 |
| POST com código duplicado → 409; sem código → 400 | ✅ OK | 0.063 |

### Autenticação e cadastro de usuários

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| POST /auth/login com usuário inexistente → 401 (sem revelar se o e-mail existe) | ✅ OK | 0.058 |
| POST /auth/login com JSON malformado → 400 | ✅ OK | 0.002 |
| POST /usuarios com senha fraca → 400 | ✅ OK | 0.002 |
| GET /auth/me sem token → 401 | ✅ OK | 0.001 |
| POST /usuarios com e-mail já cadastrado → 409 | ✅ OK | 0.003 |
| POST /auth/login com corpo inválido → 400 com lista de erros por campo | ✅ OK | 0.001 |
| POST /auth/login com senha errada → 401 padronizado | ✅ OK | 0.057 |
| POST /auth/login com credenciais válidas → 200 + JWT | ✅ OK | 0.057 |
| GET /auth/me devolve as informações contidas no token | ✅ OK | 0.059 |
| POST /usuarios (cadastro público) → 201 + Location, sempre com perfil ANALISTA | ✅ OK | 0.058 |

### Recurso /consultas

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| GET /{id} com Accept: text/csv exporta a lista padronizada | ✅ OK | 0.126 |
| POST sem token → 401 | ✅ OK | 0.001 |
| DELETE pelo dono → 204 e depois GET → 404; outro analista → 403 | ✅ OK | 0.240 |
| GET /{id} pelo dono → 200; por outro analista → 403; pelo ADMIN → 200 | ✅ OK | 0.294 |
| POST → 201 com header Location apontando para a consulta criada | ✅ OK | 0.063 |
| GET /{id} inexistente → 404 | ✅ OK | 0.059 |
| GET lista paginada: analista vê apenas as próprias consultas | ✅ OK | 0.132 |
| POST com campos obrigatórios ausentes → 400 com erros por campo (em português) | ✅ OK | 0.059 |

### Validação do desafio: Ford Ranger Raptor

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| Formato é idêntico para qualquer veículo; ausência de dados fica explícita | ✅ OK | 0.583 |
| Busca do veículo é tolerante a caixa, acentos e espaços | ✅ OK | 0.072 |
| Entrega todas as especificações da Ranger Raptor, na ordem pedida e com status DISPONIVEL | ✅ OK | 0.073 |
| Termos repetidos (mesmo após normalização) geram uma única linha | ✅ OK | 0.068 |

### Segurança: JWT, endpoints públicos/protegidos e perfis

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| ADMIN pode listar usuários e promover um analista; novo perfil vale no próximo token | ✅ OK | 0.308 |
| ANALISTA não pode criar veículo, atributo nem listar usuários → 403 | ✅ OK | 0.062 |
| Catálogo de atributos, OpenAPI e health são públicos | ✅ OK | 0.183 |
| Token malformado ou com assinatura inválida → 401 | ✅ OK | 0.062 |
| Recurso protegido sem token → 401 | ✅ OK | 0.002 |
| Token expirado → 401 com mensagem 'Token expirado' | ✅ OK | 0.003 |
| Token válido gerado com a chave do servidor é aceito (perfil vem da claim) | ✅ OK | 0.003 |
| ANALISTA pode ler veículos e realizar consultas | ✅ OK | 0.075 |

### Padronização das respostas de erro (RFC 9457)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| Todo erro traz type, title, status, detail, instance, codigo e timestamp | ✅ OK | 0.059 |
| Parâmetro de caminho com tipo inválido → 400 | ✅ OK | 0.058 |
| Rota inexistente (autenticado) → 404 no mesmo formato | ✅ OK | 0.059 |
| Content-Type não suportado → 415 no mesmo formato | ✅ OK | 0.058 |
| Método HTTP não suportado → 405 no mesmo formato | ✅ OK | 0.059 |

### Recursos /veiculos e /veiculos/{id}/especificacoes

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| GET com filtro por marca | ✅ OK | 0.071 |
| Especificação cadastrada via API passa a aparecer nas consultas | ✅ OK | 0.134 |
| Especificação com atributo inexistente → 404; analista tentando escrever → 403 | ✅ OK | 0.121 |
| POST de veículo com campos inválidos → 400 | ✅ OK | 0.066 |
| Ciclo completo do ADMIN: POST 201 → GET 200 → PUT 200 → DELETE 204 → GET 404 | ✅ OK | 0.075 |
| Especificações: PUT cria (201), PUT de novo substitui (200), GET, DELETE (204) | ✅ OK | 0.186 |
| POST de veículo já existente (ignorando caixa/acentos) → 409 | ✅ OK | 0.059 |

### JwtService (geração e validação de JWT)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| deveRejeitarTextoQueNaoEJwt | ✅ OK | 0.107 |
| deveRejeitarTokenExpirado | ✅ OK | 0.096 |
| deveRejeitarTokenAssinadoComOutraChave | ✅ OK | 0.003 |
| deveRejeitarTokenAdulterado | ✅ OK | 0.001 |
| deveGerarTokenComClaimsEExpiracaoConfigurada | ✅ OK | 0.011 |
| deveRejeitarTokenDeOutroEmissor | ✅ OK | 0.002 |

### NormalizadorTexto

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| similaridadeDeveTolerarLetrasInvertidas | ✅ OK | 0.061 |
| deveTratarNuloComoVazio | ✅ OK | 0.005 |
| similaridadeDeveSerBaixaParaTextosDiferentes | ✅ OK | 0.001 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""Potência Máx. (cv)"" → ""potencia max cv"" | ✅ OK | 0.004 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""CÂMBIO"" → ""cambio"" | ✅ OK | 0.001 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""Ângulo   de   entrada"" → ""angulo de entrada"" | ✅ OK | 0.001 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""0-100 km/h"" → ""0 100 km h"" | ✅ OK | 0.001 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""Ford"" → ""ford"" | ✅ OK | 0.001 |
| similaridadeDeveSerMaximaParaTextosIguais | ✅ OK | 0.000 |

### ReconhecedorAtributos (motor de regras)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""POTENCIA"" → "POTENCIA" | ✅ OK | 0.004 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""Potência máxima"" → "POTENCIA" | ✅ OK | 0.001 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""potencia maxima"" → "POTENCIA" | ✅ OK | 0.001 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""HP"" → "POTENCIA" | ✅ OK | 0.001 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""Câmbio"" → "TRANSMISSAO" | ✅ OK | 0.000 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""CAMBIO"" → "TRANSMISSAO" | ✅ OK | 0.000 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""Vão Livre"" → "ALTURA_SOLO" | ✅ OK | 0.000 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""altura_solo"" → "ALTURA_SOLO" | ✅ OK | 0.001 |
| O catálogo de carga não possui termos ambíguos (mesmo termo em dois atributos) | ✅ OK | 0.140 |
| deveTolerarErrosDeDigitacao(String) erro de digitação ""potencai"" | ✅ OK | 0.001 |
| deveTolerarErrosDeDigitacao(String) erro de digitação ""transmisão"" | ✅ OK | 0.001 |
| deveTolerarErrosDeDigitacao(String) erro de digitação ""altura livre do sollo"" | ✅ OK | 0.001 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""cafeteira"" não é reconhecido | ✅ OK | 0.001 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""teto solar"" não é reconhecido | ✅ OK | 0.001 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""xyz"" não é reconhecido | ✅ OK | 0.000 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""  "" não é reconhecido | ✅ OK | 0.001 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""cc"" não é reconhecido | ✅ OK | 0.000 |
