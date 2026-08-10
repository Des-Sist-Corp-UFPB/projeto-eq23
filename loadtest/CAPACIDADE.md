# Teste de Capacidade — Ponto de Ruptura (k6)

Objetivo: descobrir **quantos usuários simultâneos** o sistema aguenta mantendo o
tempo de resposta **p(95) ≤ 2000 ms**, navegando de forma autenticada e realista
(mesmo fluxo do `loadtest/carga.js`: login → home → listagem/busca de ativos →
listagem de chamados).

## Metodologia

- Script: [`loadtest/capacidade.js`](capacidade.js) — reaproveita o fluxo de
  navegação autenticada de `carga.js` (login via Spring Security + CSRF,
  sessão restaurada manualmente a cada iteração porque o k6 reseta o cookie
  jar entre iterações).
- Executor `ramping-vus`: sobe para N usuários virtuais em 10s, sustenta por
  25s, desaquece em 5s.
- Ambiente: `docker compose -f docker/docker-compose.dev.yml up` (app +
  Postgres 16 locais, configuração padrão do Spring Boot — sem tuning de
  `server.tomcat.threads` nem `spring.datasource.hikari`).
- Busca do ponto de ruptura: execuções em níveis crescentes de VUs (busca
  exponencial e depois refinada), cada uma isolada, medindo `p(95)`, taxa de
  erro e vazão (`req/s`).

## Resultados

| VUs simultâneos | p(95) | média | máximo | taxa de erro | requisições | vazão (req/s) |
|---:|---:|---:|---:|---:|---:|---:|
| 25  | 39.2 ms   | 21.5 ms  | 187.8 ms  | 0.00% | 3078 | 75.4 |
| 50  | 62.0 ms   | 26.8 ms  | 340.0 ms  | 0.00% | 6044 | 147.9 |
| 100 | 252.5 ms  | 83.3 ms  | 878.3 ms  | 0.00% | 10024 | 246.3 |
| 150 | 717.4 ms  | 260.1 ms | 2664.6 ms | 0.00% | 9900 | 241.7 |
| 200 | 1060.6 ms | 393.4 ms | 3803.6 ms | 0.00% | 10492 | 257.5 |
| **250** | **1818.9 ms** | 640.5 ms | 5657.6 ms | 0.00% | 9512 | 232.1 |
| **260** | **2006.9 ms** ⚠️ | 766.0 ms | 5971.8 ms | 0.00% | 8780 | 214.8 |
| 270 | 3039.9 ms ❌ | 1039.7 ms | 8801.9 ms | 0.00% | 7160 | 175.2 |
| 300 | 2380.1 ms ❌ | 881.6 ms | 6881.7 ms | 0.00% | 9132 | 224.5 |

## Conclusão

- **Capacidade máxima sustentável (p(95) ≤ 2s): ~250 usuários simultâneos.**
  Em 250 VUs o p(95) ainda fica em 1.82s; em 260 VUs já ultrapassa levemente
  os 2s (2.01s) e a partir daí a degradação se acentua.
- **Vazão máxima**: por volta de **230–260 requisições/segundo**. Note que a
  vazão **para de crescer** a partir de ~100 VUs (246 req/s) e praticamente
  não aumenta até 250 VUs (232 req/s) — sinal claro de que o sistema satura
  um recurso finito e passa a **enfileirar** requisições em vez de rejeitá-las.
- **Nenhum erro HTTP em nenhum nível testado** (0.00% em todos, até 300 VUs).
  Ou seja, o sistema não quebra sob carga — ele apenas fica mais lento
  (comportamento de fila, não de colapso/crash). Isso é consistente com um
  gargalo em um **pool de recursos limitado**, não em falta de memória ou
  exceções não tratadas.
- **Hipótese mais provável para o gargalo**: o projeto não customiza
  `spring.datasource.hikari.maximum-pool-size` (padrão do HikariCP: **10
  conexões**) nem `server.tomcat.threads.max` (padrão do Tomcat embutido:
  **200 threads**). Com todas as rotas do fluxo lendo do Postgres, um pool de
  apenas 10 conexões é o candidato mais forte a estar sendo saturado bem antes
  das 200 threads do Tomcat, fazendo requisições esperarem na fila do pool.
- **Recomendação (não aplicada nesta sessão)**: se o sistema precisar suportar
  mais de ~250 usuários simultâneos em produção, o primeiro ajuste a testar é
  aumentar `spring.datasource.hikari.maximum-pool-size` (e validar o limite de
  conexões do Postgres) antes de qualquer scale-out horizontal.

## Como reproduzir

```bash
# Suba o ambiente
docker compose -f docker/docker-compose.dev.yml up -d

# Rode em um nível fixo de VUs (ajuste o valor para explorar outros pontos)
k6 run -e VUS=250 -e BASE_URL=http://localhost:8080 loadtest/capacidade.js

# Ou via Docker, sem instalar k6:
docker run --rm -e BASE_URL=http://host.docker.internal:8080 -e VUS=250 \
  -v "$(pwd)/loadtest:/scripts" grafana/k6 run /scripts/capacidade.js
```
