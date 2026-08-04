# Sistema Mercado — Projeto Base DSC/UFPB

Projeto base (boilerplate) para a disciplina **Desenvolvimento de Sistemas Corporativos**.

**Professor**: Rodrigo Rebouças | **UFPB — Campus IV**

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21 + Spring Boot 3.4.5 |
| Templates | Thymeleaf + HTMX 2.0 |
| Frontend | Bootstrap 5.3 |
| Banco | PostgreSQL 16 |
| Migrações | Flyway 11 |
| Segurança | Spring Security 6 |
| Build | Maven 3.9 |
| CI/CD | GitHub Actions |

---

## Guia de Instalação para Alunos

### Passo 1 — Instale o Java 21

O projeto requer Java 21. Recomendamos o **Eclipse Temurin** (distribuição gratuita da Adoptium).

**Windows / macOS / Linux:**
1. Acesse https://adoptium.net/temurin/releases/?version=21
2. Baixe o instalador para seu sistema operacional
3. Execute o instalador e siga as instruções

**Verificar se está correto:**
```bash
java -version
# Esperado: openjdk version "21.x.x" ...
```

> **Dica para Windows:** durante a instalação, marque a opção *"Add to PATH"* e *"Set JAVA_HOME"*.

---

### Passo 2 — Instale o Maven

O Maven é a ferramenta de build do projeto.

**macOS (com Homebrew):**
```bash
brew install maven
```

**Windows:**
1. Acesse https://maven.apache.org/download.cgi
2. Baixe o arquivo `apache-maven-3.x.x-bin.zip`
3. Extraia para uma pasta (ex.: `C:\maven`)
4. Adicione `C:\maven\bin` à variável de ambiente `PATH`

**Linux (Ubuntu/Debian):**
```bash
sudo apt install maven
```

**Verificar:**
```bash
mvn -version
# Esperado: Apache Maven 3.x.x
```

---

### Passo 3 — Instale o Docker Desktop

O Docker sobe o banco de dados PostgreSQL sem precisar instalar nada manualmente.

1. Acesse https://www.docker.com/products/docker-desktop/
2. Baixe e instale o Docker Desktop para seu sistema
3. Abra o Docker Desktop e aguarde ele inicializar (ícone na barra de tarefas)

**Verificar:**
```bash
docker -v
# Esperado: Docker version 27.x.x ...
```

> **Importante:** o Docker Desktop deve estar **em execução** sempre que você for rodar o projeto.

---

### Passo 4 — Clone o repositório

```bash
git clone <URL-DO-REPOSITÓRIO>
cd base_projeto
```

> Substitua `<URL-DO-REPOSITÓRIO>` pela URL fornecida pelo professor.

---

### Passo 5 — Execute o projeto

Você tem duas opções. **Recomendamos a Opção A para a primeira execução.**

#### Opção A: Tudo com Docker (mais simples)

Um único comando sobe o banco, a aplicação e o Adminer (interface web do banco):

```bash
docker compose -f docker/docker-compose.dev.yml up --build
```

Aguarde as mensagens de inicialização. Quando aparecer algo como:
```
Started MercadoApplication in X.XXX seconds
```
...a aplicação está pronta.

#### Opção B: Banco no Docker + aplicação local (recomendado para desenvolvimento)

Esta opção permite editar o código e ver as mudanças mais rápido:

```bash
# Terminal 1 — sobe o banco de dados
docker compose -f docker/docker-compose.dev.yml up postgres adminer

# Terminal 2 — roda a aplicação (em outro terminal, na mesma pasta)
mvn spring-boot:run
```

---

### Passo 6 — Acesse no browser

| O que | Endereço |
|-------|----------|
| Aplicação | http://localhost:8080 |
| Login | usuário: `admin` / senha: `admin123` |
| Adminer (banco) | http://localhost:8888 |
| Health check | http://localhost:8080/actuator/health |

---

### Parando o projeto

```bash
# Parar a aplicação: Ctrl+C no terminal onde está rodando

# Parar os containers Docker:
docker compose -f docker/docker-compose.dev.yml down
```

---

## Solução de Problemas Comuns

### "Port 8080 already in use"
Outra aplicação está usando a porta 8080. Para liberar:
```bash
# macOS / Linux
lsof -ti:8080 | xargs kill

# Windows (PowerShell)
netstat -ano | findstr :8080
# Anote o PID da última coluna e execute:
taskkill /PID <número-do-pid> /F
```

### "Cannot connect to the Docker daemon"
O Docker Desktop não está em execução. Abra o aplicativo Docker Desktop e aguarde inicializar.

### "Connection refused" ao banco de dados
O container do PostgreSQL ainda não subiu. Aguarde alguns segundos e tente novamente. Você pode verificar com:
```bash
docker compose -f docker/docker-compose.dev.yml ps
# O container "mercado-postgres-dev" deve estar com status "healthy"
```

### Erro de compilação Java
Verifique se o Java 21 está sendo usado pelo Maven:
```bash
mvn -version
# A linha "Java version:" deve mostrar 21.x.x
```
Se mostrar outra versão, configure a variável `JAVA_HOME` apontando para o Java 21.

### Flyway: "Found non-empty schema(s) with no schema history table"
O banco existe mas foi criado sem as migrations. Apague os dados e recomece:
```bash
docker compose -f docker/docker-compose.dev.yml down -v
docker compose -f docker/docker-compose.dev.yml up postgres
```

---

## Testes

```bash
# Rodar todos os testes (requer Docker em execução — usa Testcontainers)
mvn test

# Rodar com relatório de cobertura (JaCoCo)
mvn verify
# Relatório: abra o arquivo target/site/jacoco/index.html no browser
```

---

## Análise de Segurança (SAST)

```bash
# SpotBugs + FindSecBugs + OWASP Dependency Check
mvn verify -Psecurity

# Trivy: scan de vulnerabilidades no filesystem
docker compose -f docker/docker-compose.dev.yml --profile scan up trivy

# Verificar dependências desatualizadas
mvn versions:display-dependency-updates -Pversions
```

Veja `docs/SECURITY.md` para detalhes.

---

## Configurando o Deploy Automático (GitHub Actions)

O projeto inclui um pipeline de CI/CD em `.github/workflows/deploy.yml` que:
- roda os testes automaticamente a cada `push` na branch `main`
- executa análise de segurança (SAST) no código e nas dependências
- constrói a imagem Docker de produção e faz o deploy no servidor da disciplina

Para ativar o deploy, você precisa configurar **dois secrets** e uma **variável** no seu repositório GitHub.

---

### Secret 1 — Chave SSH de deploy (`SSH_DEPLOY_KEY`)

O servidor da disciplina (`dsc.rodrigor.com`) já está preparado para receber deploys.
A chave SSH que autoriza o acesso está disponível na página da disciplina:

**Acesse: https://gd.dsc.rodrigor.com** e copie a chave SSH privada disponibilizada pelo professor.

Depois, adicione no seu repositório:

1. No GitHub, acesse seu repositório → **Settings**
2. No menu lateral: **Secrets and variables → Actions**
3. Clique em **New repository secret**
4. Nome: `SSH_DEPLOY_KEY`
5. Valor: cole a chave privada copiada do portal (o texto completo, incluindo as linhas `-----BEGIN...` e `-----END...`)
6. Clique em **Add secret**

---

### Secret 2 — Chave da API do NVD (`NVD_API_KEY`)

#### O que é o NVD?

**NVD** significa *National Vulnerability Database* — é o banco de dados oficial do governo americano (NIST) que cataloga todas as vulnerabilidades de segurança conhecidas em softwares. Cada vulnerabilidade recebe um identificador chamado **CVE** (ex.: CVE-2024-12345) e uma nota de gravidade chamada **CVSS** (de 0 a 10).

O **OWASP Dependency Check** (uma das ferramentas de segurança do projeto) consulta esse banco para verificar se as bibliotecas que o seu projeto usa possuem vulnerabilidades conhecidas.

#### Por que preciso de uma chave?

Sem a chave, o download do banco de dados NVD é muito lento (pode levar 20+ minutos no CI/CD, ou até falhar por timeout). Com a chave gratuita, o download é feito via API e leva menos de 2 minutos.

#### Como obter (gratuito, leva ~1 minuto)

1. Acesse https://nvd.nist.gov/developers/request-an-api-key
2. Preencha seu e-mail institucional (use o e-mail da UFPB se possível)
3. Marque a caixa de uso não-comercial
4. Clique em **Submit**
5. Acesse seu e-mail — você receberá a chave em segundos

#### Adicionando ao repositório

1. No GitHub: **Settings → Secrets and variables → Actions**
2. Clique em **New repository secret**
3. Nome: `NVD_API_KEY`
4. Valor: cole a chave recebida por e-mail
5. Clique em **Add secret**

> **Sem a chave ainda?** O pipeline funciona mesmo sem ela, mas o OWASP Dependency Check
> pode demorar muito ou falhar por timeout. Configure assim que possível.

---

### Variável — Nome da imagem Docker (`APP_IMAGE`)

O pipeline publica a imagem Docker no GitHub Container Registry (GHCR) com o nome do seu repositório. Você não precisa configurar isso manualmente — o workflow usa `${{ github.repository }}` para montar o nome automaticamente.

Mas o arquivo `.env` no servidor precisa saber qual imagem usar. O script de deploy atualiza isso automaticamente na primeira execução.

---

### Verificando se o deploy funcionou

Após configurar os secrets e fazer um `push` na branch `main`:

1. No GitHub, clique na aba **Actions**
2. Você verá o workflow **"Build & Deploy"** em execução
3. Ele tem 3 etapas: **Testes e SAST → Build e push → Deploy em produção**
4. Se tudo der certo, a aplicação estará disponível em `https://dsc.rodrigor.com`

Se alguma etapa falhar, clique nela para ver os logs detalhados.

---

## Estrutura do Projeto

```
base_projeto/
├── .github/workflows/
│   └── deploy.yml           # Pipeline CI/CD (GitHub Actions)
├── src/main/java/br/ufpb/dsc/mercado/
│   ├── config/              # Configurações (Security, GlobalModelAttributes, etc.)
│   ├── controller/          # Controllers HTTP + HTMX
│   ├── domain/              # Entidades JPA
│   ├── dto/                 # Data Transfer Objects (Records)
│   ├── exception/           # Exceções de domínio
│   ├── repository/          # Interfaces Spring Data JPA
│   └── service/             # Lógica de negócio
├── src/main/resources/
│   ├── db/migration/        # Scripts Flyway (V1__, V2__, ...)
│   └── templates/           # Templates Thymeleaf
├── docker/                  # Dockerfiles + docker-compose
├── docs/                    # Documentação técnica
├── CLAUDE.md                # Memória para Claude Code
└── pom.xml
```

---

## Para Alunos: Adaptando o Boilerplate

1. **Renomear** a entidade `Produto` para sua entidade principal
2. **Criar migration** Flyway com a nova estrutura da tabela (`src/main/resources/db/migration/V2__...sql`)
3. **Atualizar** Repository, Service, Controller e templates seguindo os mesmos padrões
4. **Manter** a estrutura de pacotes e convenções (ver `docs/CONVENTIONS.md`)
5. **Nunca editar** migrations já aplicadas — sempre criar uma nova (`V3__`, `V4__`, ...)

> Dúvidas? Consulte a documentação em `docs/` ou o professor.

---

## Log de Auditoria

O sistema possui um mecanismo de auditoria para rastreamento de ações relevantes executadas na aplicação.

- **O que é auditado**: Operações de gravação e remoção executadas por usuários no sistema. Atualmente, são auditadas as ações de criação (`CRIAR`), edição (`EDITAR`) e exclusão (`EXCLUIR`) de entidades do sistema, como `Ativo` e `Chamado`.
- **Onde fica armazenado**: Armazenado na tabela `log_auditoria` do banco de dados PostgreSQL. Seus principais campos são:
  - `id` (BIGINT, PRIMARY KEY): Identificador único do registro de auditoria.
  - `usuario` (VARCHAR(100), NOT NULL): Identificador/username do usuário que executou a ação (capturado automaticamente do contexto de autenticação do Spring Security). Se executado fora de um contexto autenticado, registra-se como `"sistema"`.
  - `acao` (VARCHAR(50), NOT NULL): O tipo da ação executada (ex: `CRIAR`, `EDITAR`, `EXCLUIR`).
  - `entidade` (VARCHAR(50), NOT NULL): O nome da entidade alvo da ação (ex: `Ativo`, `Chamado`).
  - `entidade_id` (BIGINT): O ID do registro da entidade no banco de dados.
  - `detalhes` (TEXT): Uma descrição textual amigável detalhando a alteração realizada.
  - `data_hora` (TIMESTAMP, NOT NULL): O carimbo de data e hora em que a ação ocorreu.
- **Como foi implementado**: A auditoria foi implementada através de um serviço dedicado `LogAuditoriaService`. A captura do usuário autenticado é feita programaticamente através da consulta à sessão activa usando o `SecurityContextHolder.getContext().getAuthentication()`. Os serviços de domínio (`AtivoService`, `ChamadoService`) chamam esse serviço de forma síncrona dentro da mesma transação de banco de dados das operações de negócio correspondentes.
- **Quais classes/arquivos participam**:
  - Entidade JPA: [LogAuditoria.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/domain/LogAuditoria.java)
  - Repositório Spring Data: [LogAuditoriaRepository.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/repository/LogAuditoriaRepository.java)
  - Serviço de Auditoria: [LogAuditoriaService.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/service/LogAuditoriaService.java)
  - Controller da Interface de Visualização: [AuditoriaController.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/controller/AuditoriaController.java)
  - Pontos de chamada nos serviços: [AtivoService.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/service/AtivoService.java) e [ChamadoService.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/service/ChamadoService.java)

---

## Integração com Serviço Externo

O sistema integra-se com serviços de terceiros para o envio de e-mails transacionais e de notificação.

- **Qual é o serviço externo**: **Resend** (uma plataforma moderna para envio de e-mails transacionais via API HTTP).
- **Para que é usado**: Envio automático de e-mails de notificação para a equipe de suporte do sistema assim que um novo chamado é criado por um cliente.
- **Quais classes/arquivos participam**:
  - Interface do serviço: [EmailService.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/service/EmailService.java)
  - Implementação real via API HTTP do Resend: [ResendEmailService.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/service/ResendEmailService.java)
  - Implementação fictícia (Mock) de desenvolvimento: [MockEmailService.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/service/MockEmailService.java)
  - Configuração do Spring Framework: [EmailConfig.java](file:///c:/Users/matheus_nelvam/Documents/projeto-eq23/src/main/java/br/ufpb/dsc/mercado/config/EmailConfig.java)
- **Como é configurado**: A integração é configurada dinamicamente com base nas propriedades no arquivo `application-dev.yml` (e possivelmente injetadas via variáveis de ambiente). Se nenhuma chave de API for fornecida, o sistema alterna automaticamente para a simulação local (`MockEmailService`). As seguintes variáveis são utilizadas:
  - `EMAIL_API_URL`: O endpoint da API do Resend (padrão: `https://api.resend.com/emails`).
  - `EMAIL_API_KEY`: A chave secreta (Token de API) do Resend necessária para autenticação via Header Bearer.
  - `EMAIL_FROM`: O endereço de e-mail do remetente (padrão: `onboarding@resend.dev`).
  - `EMAIL_TO`: O endereço de e-mail do destinatário onde as notificações de novos chamados serão entregues (padrão: `suporte@sparktech.com`).

---

## Analytics com Umami (Self-Hosted)

A aplicação oferece suporte à integração com o **Umami Analytics** para monitoramento de acessos e eventos em tempo real.

- **Modo Self-Hosted via Docker**:
  - O arquivo `docker/docker-compose.dev.yml` inclui os serviços `umami` (disponível em `http://localhost:3000`) e `umami-db` (PostgreSQL isolado para o Umami).
  - Credenciais padrão do painel Umami: `admin` / `umami`.
- **Como funciona**:
  - O script de rastreamento é injetado dinamicamente no `<head>` do `layout.html` e `login.html` via `GlobalModelAttributes` somente quando a variável `UMAMI_WEBSITE_ID` está preenchida.
- **Rastreamento de Eventos Personalizados**:
  - Os formulários e botões principais contêm atributos `data-umami-event`:
    - `login-form`: Submissão do formulário de login por senha
    - `login-google`: Autenticação via Google OIDC
    - `salvar-chamado`: Abertura e edição de chamados
    - `salvar-ativo`: Cadastro e atualização de ativos
- **Variáveis de ambiente**:
  - `UMAMI_SCRIPT_URL`: URL do script de rastreamento (padrão local: `http://localhost:3000/script.js`).
  - `UMAMI_WEBSITE_ID`: ID do site gerado no painel do Umami. Se estiver vazio ou omitido, o rastreamento é omitido automaticamente.
