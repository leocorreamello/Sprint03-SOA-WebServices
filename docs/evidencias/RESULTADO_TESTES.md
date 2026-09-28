# Evidência de execução dos testes automatizados

- **Data da execução:** 27/09/2026 20:19
- **Ambiente:** Windows 11 · openjdk version "25.0.3" 2026-04-21 LTS
- **Comando:** `.\mvnw.cmd -q verify`

## Resumo

| Total | Sucesso | Falhas | Ignorados |
|---:|---:|---:|---:|
| 84 | 84 | 0 | 0 |

## Cobertura de código (JaCoCo)

| Métrica | Cobertura |
|---|---:|
| Instruções | 94.5% (4051/4286) |
| Linhas | 94.6% (748/791) |
| Branches | 79.0% (132/167) |
| Métodos | 92.7% (268/289) |

Relatório completo: `target/site/jacoco/index.html` · Relatório HTML dos testes: `target/reports/surefire.html`

## Resultado por suíte

| Suíte | Classe | Testes | Falhas | Tempo (s) |
|---|---|---:|---:|---:|
| Recurso /atributos (catálogo) | `AtributoIT` | 6 | 0 | 14.42 |
| Autenticação e cadastro de usuários | `AutenticacaoIT` | 11 | 0 | 0.81 |
| Recurso /consultas | `ConsultaIT` | 8 | 0 | 1.41 |
| Monitoramento local com Prometheus | `MonitoramentoIT` | 1 | 0 | 3.25 |
| Validação do desafio: Ford Ranger Raptor | `RangerRaptorValidacaoIT` | 4 | 0 | 0.68 |
| Segurança: JWT, endpoints públicos/protegidos e perfis | `SegurancaIT` | 9 | 0 | 1.37 |
| Padronização das respostas de erro (RFC 9457) | `TratamentoErrosIT` | 5 | 0 | 0.39 |
| Recursos /veiculos e /veiculos/{id}/especificacoes | `VeiculoEspecificacaoIT` | 7 | 0 | 0.91 |
| JwtService (geração e validação de JWT) | `JwtServiceTest` | 6 | 0 | 0.03 |
| br.com.fiap.autointel.unit.LoginThrottleTest | `LoginThrottleTest` | 1 | 0 | 0.01 |
| NormalizadorTexto | `NormalizadorTextoTest` | 9 | 0 | 0.07 |
| ReconhecedorAtributos (motor de regras) | `ReconhecedorAtributosTest` | 17 | 0 | 0.04 |

## Detalhamento dos cenários

### Recurso /atributos (catálogo)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| POST sem token → 401 (escrita no catálogo não é pública) | ✅ OK | 0.636 |
| DELETE de atributo com especificações cadastradas → 409 | ✅ OK | 0.598 |
| GET público lista o catálogo e permite filtrar por categoria | ✅ OK | 0.146 |
| GET /{codigo} público → 200; inexistente → 404; categoria inválida → 400 | ✅ OK | 0.023 |
| ADMIN cria (201), atualiza (200) e remove (204) atributo; novo sinônimo já é reconhecido | ✅ OK | 0.366 |
| POST com código duplicado → 409; sem código → 400 | ✅ OK | 0.092 |

### Autenticação e cadastro de usuários

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| POST /auth/login com usuário inexistente → 401 (sem revelar se o e-mail existe) | ✅ OK | 0.074 |
| POST /auth/login com JSON malformado → 400 | ✅ OK | 0.012 |
| POST /usuarios com senha fraca → 400 | ✅ OK | 0.044 |
| GET /auth/me sem token → 401 | ✅ OK | 0.006 |
| POST /usuarios com e-mail já cadastrado → 409 | ✅ OK | 0.009 |
| POST /auth/login com corpo inválido → 400 com lista de erros por campo | ✅ OK | 0.009 |
| POST /auth/login bloqueia a sexta falha da mesma origem → 429 | ✅ OK | 0.341 |
| POST /auth/login com senha errada → 401 padronizado | ✅ OK | 0.071 |
| POST /auth/login com credenciais válidas → 200 + JWT | ✅ OK | 0.073 |
| GET /auth/me devolve as informações contidas no token | ✅ OK | 0.075 |
| POST /usuarios (cadastro público) → 201 + Location, sempre com perfil ANALISTA | ✅ OK | 0.071 |

### Recurso /consultas

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| GET /{id} com Accept: text/csv exporta a lista padronizada | ✅ OK | 0.205 |
| POST sem token → 401 | ✅ OK | 0.004 |
| DELETE pelo dono → 204 e depois GET → 404; outro analista → 403 | ✅ OK | 0.321 |
| GET /{id} pelo dono → 200; por outro analista → 403; pelo ADMIN → 200 | ✅ OK | 0.386 |
| POST → 201 com header Location apontando para a consulta criada | ✅ OK | 0.096 |
| GET /{id} inexistente → 404 | ✅ OK | 0.075 |
| GET lista paginada: analista vê apenas as próprias consultas | ✅ OK | 0.211 |
| POST com campos obrigatórios ausentes → 400 com erros por campo (em português) | ✅ OK | 0.093 |

### Monitoramento local com Prometheus

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| prometheusExpostoApenasNoPerfilMonitor | ✅ OK | 1.182 |

### Validação do desafio: Ford Ranger Raptor

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| Formato é idêntico para qualquer veículo; ausência de dados fica explícita | ✅ OK | 0.395 |
| Busca do veículo é tolerante a caixa, acentos e espaços | ✅ OK | 0.087 |
| Entrega todas as especificações da Ranger Raptor, na ordem pedida e com status DISPONIVEL | ✅ OK | 0.098 |
| Termos repetidos (mesmo após normalização) geram uma única linha | ✅ OK | 0.086 |

### Segurança: JWT, endpoints públicos/protegidos e perfis

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| Métricas exigem ADMIN; ANALISTA recebe 403 | ✅ OK | 0.145 |
| ADMIN pode listar usuários e promover um analista; novo perfil vale no próximo token | ✅ OK | 0.362 |
| ANALISTA não pode criar veículo, atributo nem listar usuários → 403 | ✅ OK | 0.076 |
| Catálogo de atributos, OpenAPI e health são públicos | ✅ OK | 0.563 |
| Token malformado ou com assinatura inválida → 401 | ✅ OK | 0.076 |
| Recurso protegido sem token → 401 | ✅ OK | 0.004 |
| Token expirado → 401 com mensagem 'Token expirado' | ✅ OK | 0.008 |
| Token válido gerado com a chave do servidor é aceito (perfil vem da claim) | ✅ OK | 0.008 |
| ANALISTA pode ler veículos e realizar consultas | ✅ OK | 0.107 |

### Padronização das respostas de erro (RFC 9457)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| Todo erro traz type, title, status, detail, instance, codigo e timestamp | ✅ OK | 0.074 |
| Parâmetro de caminho com tipo inválido → 400 | ✅ OK | 0.072 |
| Rota inexistente (autenticado) → 404 no mesmo formato | ✅ OK | 0.074 |
| Content-Type não suportado → 415 no mesmo formato | ✅ OK | 0.072 |
| Método HTTP não suportado → 405 no mesmo formato | ✅ OK | 0.082 |

### Recursos /veiculos e /veiculos/{id}/especificacoes

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| GET com filtro por marca | ✅ OK | 0.083 |
| Especificação cadastrada via API passa a aparecer nas consultas | ✅ OK | 0.175 |
| Especificação com atributo inexistente → 404; analista tentando escrever → 403 | ✅ OK | 0.147 |
| POST de veículo com campos inválidos → 400 | ✅ OK | 0.070 |
| Ciclo completo do ADMIN: POST 201 → GET 200 → PUT 200 → DELETE 204 → GET 404 | ✅ OK | 0.104 |
| Especificações: PUT cria (201), PUT de novo substitui (200), GET, DELETE (204) | ✅ OK | 0.240 |
| POST de veículo já existente (ignorando caixa/acentos) → 409 | ✅ OK | 0.073 |

### JwtService (geração e validação de JWT)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| deveRejeitarTextoQueNaoEJwt | ✅ OK | 0.003 |
| deveRejeitarTokenExpirado | ✅ OK | 0.003 |
| deveRejeitarTokenAssinadoComOutraChave | ✅ OK | 0.002 |
| deveRejeitarTokenAdulterado | ✅ OK | 0.002 |
| deveGerarTokenComClaimsEExpiracaoConfigurada | ✅ OK | 0.011 |
| deveRejeitarTokenDeOutroEmissor | ✅ OK | 0.002 |

### br.com.fiap.autointel.unit.LoginThrottleTest

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| bloqueiaSprayEntreContasDiferentesDaMesmaOrigem | ✅ OK | 0.005 |

### NormalizadorTexto

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| similaridadeDeveTolerarLetrasInvertidas | ✅ OK | 0.003 |
| deveTratarNuloComoVazio | ✅ OK | 0.001 |
| similaridadeDeveSerBaixaParaTextosDiferentes | ✅ OK | 0.003 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""Potência Máx. (cv)"" → ""potencia max cv"" | ✅ OK | 0.007 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""CÂMBIO"" → ""cambio"" | ✅ OK | 0.000 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""Ângulo   de   entrada"" → ""angulo de entrada"" | ✅ OK | 0.000 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""0-100 km/h"" → ""0 100 km h"" | ✅ OK | 0.000 |
| deveNormalizarAcentosCaixaEPontuacao(String, String) ""Ford"" → ""ford"" | ✅ OK | 0.000 |
| similaridadeDeveSerMaximaParaTextosIguais | ✅ OK | 0.000 |

### ReconhecedorAtributos (motor de regras)

| Cenário | Resultado | Tempo (s) |
|---|---|---:|
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""POTENCIA"" → "POTENCIA" | ✅ OK | 0.007 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""Potência máxima"" → "POTENCIA" | ✅ OK | 0.000 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""potencia maxima"" → "POTENCIA" | ✅ OK | 0.001 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""HP"" → "POTENCIA" | ✅ OK | 0.002 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""Câmbio"" → "TRANSMISSAO" | ✅ OK | 0.000 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""CAMBIO"" → "TRANSMISSAO" | ✅ OK | 0.000 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""Vão Livre"" → "ALTURA_SOLO" | ✅ OK | 0.001 |
| deveReconhecerPorCodigoNomeOuSinonimo(String, String) ""altura_solo"" → "ALTURA_SOLO" | ✅ OK | 0.001 |
| O catálogo de carga não possui termos ambíguos (mesmo termo em dois atributos) | ✅ OK | 0.005 |
| deveTolerarErrosDeDigitacao(String) erro de digitação ""potencai"" | ✅ OK | 0.000 |
| deveTolerarErrosDeDigitacao(String) erro de digitação ""transmisão"" | ✅ OK | 0.000 |
| deveTolerarErrosDeDigitacao(String) erro de digitação ""altura livre do sollo"" | ✅ OK | 0.000 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""cafeteira"" não é reconhecido | ✅ OK | 0.000 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""teto solar"" não é reconhecido | ✅ OK | 0.000 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""xyz"" não é reconhecido | ✅ OK | 0.000 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""  "" não é reconhecido | ✅ OK | 0.001 |
| naoDeveReconhecerTermosForaDoCatalogo(String) ""cc"" não é reconhecido | ✅ OK | 0.001 |
