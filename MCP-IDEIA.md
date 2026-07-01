# Ideia de Servidor MCP — EQ23

**Domínio:** Gestão de ativos e chamados / help desk (Resend)  
**Data:** 2026-07-01

## O que é

Um **servidor MCP (Model Context Protocol)** expõe as operações do seu sistema como *tools* e *resources* que qualquer assistente de IA (Claude Desktop, Cursor, etc.) pode chamar com segurança. Na prática, é uma camada fina sobre a **API que vocês já têm** — cada tool chama um endpoint/service existente. Assim o projeto deixa de ser só uma tela e passa a ser operável por um agente de IA.

## Servidor proposto: `chamados-mcp`

### Tools sugeridas

- `abrir_chamado(descricao, ativoId)` — abre chamado
- `consultar_ativo(id)` — dados do ativo
- `status_chamado(id)` — situação
- `atribuir_tecnico(chamadoId, tecnicoId)` — atribui

### Resources (somente leitura)

- fila de chamados e inventário de ativos como resource

### Exemplos de uso com um LLM

- "Abra um chamado: impressora da sala 3 sem toner, e atribua ao técnico de plantão."

## Esqueleto para começar (Java / Spring AI)

```java
// pom.xml: org.springframework.ai:spring-ai-starter-mcp-server-webmvc
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ChamadosTools {

    private final SeuService seuService;   // injete seus services/repositories

    public ChamadosTools(SeuService seuService) { this.seuService = seuService; }

    @Tool(description = "abre chamado")
    public Object abrir_chamado(/* params */) {
        return seuService.suaOperacaoExistente();   // reaproveite sua lógica
    }
}
```
> Registre as tools com um `MethodToolCallbackProvider` (bean) apontando para esta classe.

## Boas práticas

- **Segurança:** cada tool que altera dados deve exigir autenticação e registrar no **log de auditoria** (o mesmo do requisito da disciplina).
- **Escopo mínimo:** exponha só o necessário; separe tools de leitura das de escrita.
- **Reaproveite:** as tools devem chamar seus *services*/*controllers* existentes, não reimplementar regra de negócio.

## Referências
- Documentação MCP: https://modelcontextprotocol.io
- SDKs: Python (`mcp`), TypeScript (`@modelcontextprotocol/sdk`), Java (Spring AI MCP Server).

*Sugestão gerada em 2026-07-01 para orientar a integração de LLMs ao projeto.*