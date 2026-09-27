# Monitoramento local AutoIntel

Stack de demonstração para a Sprint 3: API Spring Boot, Prometheus e Grafana. O perfil `monitor` habilita `/actuator/prometheus` apenas para esta stack. A API, o Prometheus e o Grafana são publicados somente em `127.0.0.1`; não use este perfil sem isolamento de rede em produção. O perfil `dev` adiciona contas e dados de demonstração.

## Iniciar no Windows

Execute `./mvnw.cmd -q -DskipTests package` na pasta `backend`. Depois, nesta pasta `monitoring`, execute `./iniciar-grafana.ps1`. O script cria uma chave JWT e uma senha de administrador locais em `.env` (ignorado pelo Git) e inicia os três contêineres.

- Grafana: http://localhost:13000/d/autointel-security/autointel-seguranca-api
- Prometheus: http://localhost:19090/targets
- API: http://localhost:18080/actuator/health

O painel é provisionado em `grafana/autointel.json`; sua fonte é o Prometheus que coleta `/actuator/prometheus` a cada 5 segundos. Para parar: `docker compose down`. A stack é uma evidência local reproduzível, não um deploy de produção. O painel mostra tráfego, erros, latência e disponibilidade; alertas/retention corporativos ainda exigem configuração operacional.
