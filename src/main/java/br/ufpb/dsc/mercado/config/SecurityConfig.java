package br.ufpb.dsc.mercado.config;

import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.security.McpApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpStatus;

/**
 * Configuração de segurança da aplicação usando Spring Security 6.
 *
 * <p><strong>Como o Spring Security funciona?</strong><br>
 * O Spring Security é baseado em uma cadeia de filtros (Filter Chain) que intercepta
 * todas as requisições HTTP antes de chegarem ao Controller. Cada filtro tem uma
 * responsabilidade específica (autenticação, autorização, CSRF, etc.).
 *
 * <p><strong>Principais conceitos:</strong>
 * <ul>
 *   <li><strong>Authentication</strong>: Verifica quem é o usuário (login/senha).</li>
 *   <li><strong>Authorization</strong>: Verifica o que o usuário pode fazer (roles/permissões).</li>
 *   <li><strong>CSRF</strong>: Proteção contra Cross-Site Request Forgery.</li>
 *   <li><strong>PasswordEncoder</strong>: Nunca armazene senhas em texto puro! BCrypt aplica um
 *       hash com salt aleatório a cada chamada.</li>
 * </ul>
 *
 * <p><strong>{@code @Configuration} + {@code @EnableWebSecurity}:</strong><br>
 * {@code @Configuration} marca a classe como fonte de definição de beans.
 * {@code @EnableWebSecurity} ativa a integração do Spring Security com o contexto do Spring MVC.
 *
 * @author DSC - UFPB Campus IV
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final br.ufpb.dsc.mercado.service.CustomOidcUserService customOidcUserService;
    private final UsuarioRepository usuarioRepository;

    @Value("${mcp.api-key:}")
    private String mcpApiKey;

    public SecurityConfig(@org.springframework.context.annotation.Lazy br.ufpb.dsc.mercado.service.CustomOidcUserService customOidcUserService,
                           UsuarioRepository usuarioRepository) {
        this.customOidcUserService = customOidcUserService;
        this.usuarioRepository = usuarioRepository;
    }


    /**
     * Define o algoritmo de codificação de senhas.
     *
     * <p><strong>Por que BCrypt?</strong><br>
     * BCrypt é um algoritmo de hash adaptativo — você pode aumentar o "cost factor"
     * conforme os computadores ficam mais rápidos, sem precisar re-hashear as senhas.
     * Ele também adiciona um salt aleatório automaticamente, impedindo ataques de
     * rainbow table (tabelas pré-computadas de hashes).
     *
     * <p>Nunca use MD5, SHA-1 ou SHA-256 simples para senhas!
     *
     * @return instância do BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura a cadeia de filtros de segurança HTTP.
     *
     * <p>Este é o método central da configuração do Spring Security.
     * A API fluente do {@code HttpSecurity} permite configurar:
     * <ul>
     *   <li>Quais URLs são públicas e quais exigem autenticação</li>
     *   <li>Como o login é feito (formulário, OAuth2, JWT, etc.)</li>
     *   <li>Como o logout funciona</li>
     *   <li>Configurações de CSRF, headers de segurança, etc.</li>
     * </ul>
     *
     * @param http construtor de configuração de segurança HTTP
     * @return cadeia de filtros configurada
     * @throws Exception se ocorrer erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // === AUTORIZAÇÃO DE REQUISIÇÕES ===
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos e health check são públicos
                        // /webjars/** → Bootstrap, HTMX (servidos pelo Spring como recursos estáticos)
                        // /css/**, /js/** → arquivos estáticos personalizados
                        // /actuator/health → monitoramento sem autenticação
                        // /ping → endpoint exigido pelo painel da disciplina
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/actuator/health", "/ping", "/cadastro").permitAll()
                        .requestMatchers("/ativos/**", "/auditoria/**", "/admin/**").hasRole("ADMIN")
                        // /mcp/** é autenticado por chave de serviço (McpApiKeyAuthFilter), não por sessão de usuário
                        .requestMatchers("/mcp/**").authenticated()
                        // Qualquer outra requisição exige autenticação
                        .anyRequest().authenticated()
                )

                // === FORMULÁRIO DE LOGIN ===
                .formLogin(form -> form
                        // URL da página de login customizada (em vez da padrão do Spring Security)
                        .loginPage("/login")
                        // Após login bem-sucedido, redireciona para o dashboard
                        .defaultSuccessUrl("/", true)
                        // A página de login deve ser acessível sem autenticação
                        .permitAll()
                )

                // === OAUTH2 LOGIN (GOOGLE) ===
                .oauth2Login(oauth2 -> {
                    oauth2.loginPage("/login");
                    if (customOidcUserService != null) {
                        oauth2.userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        );
                    }
                    oauth2.defaultSuccessUrl("/", true);
                })

                // === LOGOUT ===
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout"))
                        // Após logout, redireciona diretamente para a página de login (/login)
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )

                // === CSRF (Cross-Site Request Forgery) ===
                // Habilitado globalmente. A integração com HTMX é feita via cabeçalhos injetados nas requisições.
                // /mcp/** é ignorado porque é uma API chamada por clientes MCP (não navegador/sessão) e
                // autenticada por chave de serviço — CSRF não se aplica a esse tipo de cliente.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/mcp/**"))
                //
                // === /mcp/** SEM CHAVE VÁLIDA: 401 em vez de redirecionar para /login ===
                // Sem isso, o comportamento padrão do formLogin redireciona (302) qualquer
                // requisição não autenticada para a página HTML de login — errado para um
                // cliente MCP, que espera uma resposta HTTP simples (401), não uma página.
                .exceptionHandling(handling -> handling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/mcp/**"))
                )
                //
                // === HEADERS DE SEGURANÇA E CSP ===
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; script-src 'self' 'unsafe-inline';")
                        )
                )
                // Autentica /mcp/** via chave de serviço antes do filtro de login por formulário
                .addFilterBefore(new McpApiKeyAuthFilter(mcpApiKey, usuarioRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expõe o {@code AuthenticationManager} como bean do Spring.
     *
     * <p>Necessário quando você precisa injetar o {@code AuthenticationManager} em outras classes,
     * como em um controller de API REST que faz autenticação programática.
     * Para este projeto educacional, serve como exemplo de como expor o bean.
     *
     * @param config configuração de autenticação gerenciada pelo Spring Security
     * @return instância do AuthenticationManager
     * @throws Exception se ocorrer erro ao obter o manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
