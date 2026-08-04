package br.ufpb.dsc.mercado.mcp;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.domain.Chamado;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChamadoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.exception.ChamadoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.service.AtivoService;
import br.ufpb.dsc.mercado.service.ChamadoService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tools MCP que expõem as operações de chamados/ativos já existentes no sistema
 * para um assistente de IA (ver MCP-IDEIA.md). Cada tool chama o service já usado
 * pelos controllers — não reimplementa regra de negócio nem contorna a auditoria.
 *
 * <p>Quem chama essas tools é autenticado por {@link br.ufpb.dsc.mercado.security.McpApiKeyAuthFilter}
 * como o usuário de sistema "mcp-agent" (migration V13), então toda ação fica registrada
 * no log de auditoria sob esse usuário — dá pra distinguir o que foi feito por um humano
 * do que foi feito via assistente de IA.
 */
@Service
public class ChamadoMcpTools {

    private final ChamadoService chamadoService;
    private final AtivoService ativoService;
    private final UsuarioRepository usuarioRepository;

    public ChamadoMcpTools(ChamadoService chamadoService, AtivoService ativoService, UsuarioRepository usuarioRepository) {
        this.chamadoService = chamadoService;
        this.ativoService = ativoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Tool(description = "Abre um novo chamado de suporte técnico. Use quando o usuário pedir para registrar um " +
            "problema ou solicitar suporte técnico.")
    public String abrirChamado(
            @ToolParam(description = "Título curto do chamado, entre 5 e 100 caracteres") String titulo,
            @ToolParam(description = "Descrição detalhada do problema relatado") String descricao,
            @ToolParam(description = "Prioridade: BAIXA, MEDIA, ALTA ou CRITICA") String prioridade,
            @ToolParam(description = "ID do patrimônio (equipamento específico) relacionado ao chamado, se conhecido", required = false) Long patrimonioId) {

        Usuario solicitante = usuarioAutenticado();
        ChamadoForm form = new ChamadoForm(titulo, descricao, prioridade, "ABERTO", null, null, null, patrimonioId, null);
        Chamado chamado = chamadoService.criar(form, solicitante);
        return "Chamado #" + chamado.getId() + " aberto com sucesso: \"" + chamado.getTitulo()
                + "\" (prioridade " + chamado.getPrioridade() + ")";
    }

    // @Transactional aqui (e nos outros tools abaixo que tocam associações lazy) é necessário porque
    // a chamada MCP não roda na mesma sessão/thread do request HTTP original — sem isso, acessar uma
    // coleção/associação @Lazy depois que o service retorna estoura LazyInitializationException.
    @Tool(description = "Consulta os dados de um ativo (equipamento de TI) cadastrado, pelo ID.")
    @Transactional(readOnly = true)
    public String consultarAtivo(@ToolParam(description = "ID do ativo") Long ativoId) {
        try {
            Ativo ativo = ativoService.buscarPorId(ativoId);
            return "Ativo #" + ativo.getId() + ": " + ativo.getNome()
                    + " — status: " + ativo.getStatus()
                    + ", patrimônios cadastrados: " + ativo.getPatrimonios().size();
        } catch (AtivoNaoEncontradoException e) {
            return "Ativo não encontrado com id " + ativoId;
        }
    }

    @Tool(description = "Consulta a situação atual de um chamado pelo ID: status, prioridade e técnico responsável.")
    @Transactional(readOnly = true)
    public String statusChamado(@ToolParam(description = "ID do chamado") Long chamadoId) {
        try {
            Chamado chamado = chamadoService.buscarPorId(chamadoId);
            String tecnico = chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "não atribuído";
            return "Chamado #" + chamado.getId() + " \"" + chamado.getTitulo() + "\" — status: " + chamado.getStatus()
                    + ", prioridade: " + chamado.getPrioridade() + ", técnico: " + tecnico;
        } catch (ChamadoNaoEncontradoException e) {
            return "Chamado não encontrado com id " + chamadoId;
        }
    }

    @Tool(description = "Atribui um técnico a um chamado existente, pelos IDs do chamado e do técnico.")
    @Transactional
    public String atribuirTecnico(
            @ToolParam(description = "ID do chamado") Long chamadoId,
            @ToolParam(description = "ID do usuário técnico a ser atribuído") Long tecnicoId) {
        usuarioAutenticado(); // garante SecurityContext nessa thread p/ a auditoria atribuir a "mcp-agent"
        try {
            Chamado chamado = chamadoService.atribuirTecnico(chamadoId, tecnicoId);
            String tecnico = chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "ninguém";
            return "Chamado #" + chamado.getId() + " atribuído a " + tecnico + ".";
        } catch (ChamadoNaoEncontradoException e) {
            return "Chamado não encontrado com id " + chamadoId;
        }
    }

    // O servidor MCP processa cada mensagem numa thread própria, que não herda o SecurityContext
    // definido pelo McpApiKeyAuthFilter na thread do request HTTP original. Sem re-autenticar
    // explicitamente aqui, o LogAuditoriaService (que também lê o SecurityContext) não acha
    // autenticação nenhuma e grava a ação como "sistema" em vez de "mcp-agent" na auditoria.
    private Usuario usuarioAutenticado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }

        Usuario usuarioMcp = usuarioRepository.findByUsername("mcp-agent")
                .orElseThrow(() -> new IllegalStateException("Usuário de sistema 'mcp-agent' não encontrado — rode as migrations do Flyway."));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuarioMcp, null, usuarioMcp.getAuthorities()));
        return usuarioMcp;
    }
}
