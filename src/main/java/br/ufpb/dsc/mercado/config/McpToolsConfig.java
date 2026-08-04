package br.ufpb.dsc.mercado.config;

import br.ufpb.dsc.mercado.mcp.ChamadoMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra as tools MCP (ver {@link ChamadoMcpTools}) no servidor MCP do Spring AI.
 */
@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider chamadoMcpToolCallbackProvider(ChamadoMcpTools chamadoMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(chamadoMcpTools)
                .build();
    }
}
