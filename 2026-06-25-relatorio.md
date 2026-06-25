# Relatório de Avaliação — EQ23 (DSC)

| | |
|---|---|
| **Data** | 2026-06-25 |
| **Repositório** | https://github.com/des-sist-corp-ufpb/projeto-eq23 |
| **Aplicação** | https://eq23.dsc.rodrigor.com |
| **Período de atividade** | 2026-06-10 → 2026-06-10 |
| **Total de commits** (sem merges) | 1 |
| **Integrantes** | Matheus Nelvam Lucas (@MatheusNelvam) |

---

## 1. Tecnologias

- Spring Boot 3.4.5
- Thymeleaf
- Flyway (9 migrations)
- Spring Security
- Testcontainers

---

## 2. Análise Funcional

### Endpoints REST (20 mapeados)

| Método | Path | Arquivo |
|--------|------|---------|
| `DELETE` | `/ativos/{id}` | `AtivoController.java` |
| `GET` | `/ativos` | `AtivoController.java` |
| `GET` | `/ativos/fragmento-tabela` | `AtivoController.java` |
| `GET` | `/ativos/novo` | `AtivoController.java` |
| `GET` | `/ativos/{id}/editar` | `AtivoController.java` |
| `POST` | `/ativos` | `AtivoController.java` |
| `PUT` | `/ativos/{id}` | `AtivoController.java` |
| `GET` | `/login` | `AuthController.java` |
| `DELETE` | `/chamados/{id}` | `ChamadoController.java` |
| `GET` | `/chamados` | `ChamadoController.java` |
| `GET` | `/chamados/abrir` | `ChamadoController.java` |
| `GET` | `/chamados/novo` | `ChamadoController.java` |
| `GET` | `/chamados/{id}/editar` | `ChamadoController.java` |
| `POST` | `/chamados` | `ChamadoController.java` |
| `POST` | `/chamados/abrir` | `ChamadoController.java` |
| `PUT` | `/chamados/{id}` | `ChamadoController.java` |
| `PUT` | `/chamados/{id}/atribuir` | `ChamadoController.java` |
| `PUT` | `/chamados/{id}/status` | `ChamadoController.java` |
| `GET` | `/` | `HomeController.java` |
| `GET` | `/ping` | `PingController.java` |

### Entidades / Tabelas (7 encontradas)

- `ativo`
- `chamado`
- `usuario`
- `usuario (via V4__criar_tabela_usuario.sql)`
- `categoria (via V2__criar_tabela_categoria.sql)`
- `chamado (via V7__criar_sistema_helpdesk.sql)`
- `produto (via V1__criar_tabela_produto.sql)`

### Migrations (9 arquivos)

- `V1__criar_tabela_produto.sql`
- `V2__criar_tabela_categoria.sql`
- `V3__adicionar_categoria_e_estoque_produto.sql`
- `V4__criar_tabela_usuario.sql`
- `V5__corrigir_senha_admin.sql`
- `V6__corrigir_senha_admin_producao.sql`
- `V7__criar_sistema_helpdesk.sql`
- `V8__remover_campos_ativos.sql`
- `V9__remover_tabela_categoria.sql`

---

## 3. Análise Arquitetural

| Aspecto | Status | Observação |
|---------|--------|-----------|
| Arquitetura em camadas | ✅ | controller=✅  service=✅  repository=✅ |
| Testes automatizados | ✅ | 3 arquivo(s) de teste |
| Migrations versionadas | ✅ | 9 migration(s) |
| Logging | ❌ | não detectado |
| Autenticação / Segurança | ✅ | Spring Security / JWT / decorator detectado |
| DTOs / Separação de dados | ❌ | não detectado |
| Tratamento global de exceções | ✅ | @ControllerAdvice / @ExceptionHandler detectado |
| Documentação de API (OpenAPI) | ❌ | não detectado |
| Variáveis de ambiente | ❌ | não detectado |
| Dockerfile / docker-compose | ❌ | não encontrado |

---

## 4. Contribuição por Usuário

### Resumo

| Usuário | Commits | % commits | Linhas adicionadas | Linhas no código atual | % código atual |
|---------|---------|-----------|-------------------|----------------------|----------------|
| Matheus Nelvam Lucas (@MatheusNelvam) | 1 | 100% | 11.905 | 3.759 | 100% |

### Contribuição por Camada

| Camada | Total linhas | Matheus Nelvam Lucas (@MatheusNelvam) |
|--------|-------------|---------|
| Controller | 2.627 | 100% |
| Repository | 48 | 100% |
| Service | 458 | 100% |

---

## 5. Contribuição por Funcionalidade

Baseado em `git blame` nos arquivos de controller e service.

| Arquivo | Total linhas | Matheus Nelvam Lucas (@MatheusNelvam) |
|---------|-------------|---------|
| `layout.html` | 604 | 100% |
| `form.html` | 367 | 100% |
| `login.html` | 286 | 100% |
| `ChamadoController.java` | 202 | 100% |
| `linha.html` | 162 | 100% |
| `tabela.html` | 161 | 100% |
| `abrir.html` | 142 | 100% |
| `AtivoControllerTest.java` | 141 | 100% |
| `AtivoController.java` | 136 | 100% |
| `AtivoServiceTest.java` | 134 | 100% |
| `lista.html` | 123 | 100% |
| `ChamadoService.java` | 104 | 100% |
| `home.html` | 99 | 100% |
| `AtivoService.java` | 70 | 100% |
| `MercadoApplicationTests.java` | 53 | 100% |
| `AuthController.java` | 48 | 100% |
| `MercadoApplication.java` | 40 | 100% |
| `DashboardService.java` | 31 | 100% |
| `UserDetailsServiceImpl.java` | 26 | 100% |
| `HomeController.java` | 26 | 100% |
| `V1__criar_tabela_produto.sql` | 25 | 100% |
| `PingController.java` | 24 | 100% |
| `V7__criar_sistema_helpdesk.sql` | 21 | 100% |
| `V2__criar_tabela_categoria.sql` | 16 | 100% |
| `V3__adicionar_categoria_e_estoque_produto.sql` | 15 | 100% |
| `V4__criar_tabela_usuario.sql` | 12 | 100% |
| `V5__corrigir_senha_admin.sql` | 6 | 100% |
| `V6__corrigir_senha_admin_producao.sql` | 5 | 100% |
| `V8__remover_campos_ativos.sql` | 4 | 100% |
| `V9__remover_tabela_categoria.sql` | 2 | 100% |

---

*Relatório gerado automaticamente em 2026-06-25.*
*Os dados de contribuição são baseados em `git log --numstat` (linhas adicionadas) e `git blame` (linhas no código atual), excluindo commits de merge.*