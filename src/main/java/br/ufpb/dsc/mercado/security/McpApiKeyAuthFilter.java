package br.ufpb.dsc.mercado.security;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Autentica requisições ao servidor MCP (/mcp/**) via header "Authorization: Bearer &lt;chave&gt;".
 *
 * <p>Não é login de usuário: é uma chave de serviço única (mcp.api-key) que representa
 * "um assistente de IA autorizado a operar o sistema". Toda ação feita via MCP fica registrada
 * no log de auditoria sob o usuário de sistema "mcp-agent" (ver migration V13), para rastreabilidade.
 */
public class McpApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String USUARIO_MCP = "mcp-agent";

    private final String apiKeyConfigurada;
    private final UsuarioRepository usuarioRepository;

    public McpApiKeyAuthFilter(String apiKeyConfigurada, UsuarioRepository usuarioRepository) {
        this.apiKeyConfigurada = apiKeyConfigurada;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {

        String header = request.getHeader("Authorization");
        String chaveRecebida = (header != null && header.startsWith("Bearer "))
                ? header.substring("Bearer ".length())
                : null;

        if (chaveRecebida != null && chavesIguais(chaveRecebida, apiKeyConfigurada)) {
            Usuario usuarioMcp = usuarioRepository.findByUsername(USUARIO_MCP).orElse(null);
            if (usuarioMcp != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        usuarioMcp, null, usuarioMcp.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean chavesIguais(String a, String b) {
        if (a == null || b == null || b.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
