package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogAuditoriaService — Testes Unitários")
class LogAuditoriaServiceTest {

    @Mock
    private LogAuditoriaRepository logAuditoriaRepository;

    @InjectMocks
    private LogAuditoriaService logAuditoriaService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    @DisplayName("listar: deve retornar página de logs de auditoria ordenada")
    void listar_deveRetornarPaginaDeLogs() {
        PageRequest pageable = PageRequest.of(0, 10);
        LogAuditoria log = new LogAuditoria("admin", "CRIAR", "Ativo", 1L, "Detalhe", null);
        Page<LogAuditoria> pagina = new PageImpl<>(List.of(log));

        when(logAuditoriaRepository.findAllByOrderByDataHoraDesc(pageable)).thenReturn(pagina);

        Page<LogAuditoria> resultado = logAuditoriaService.listar(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getUsuario()).isEqualTo("admin");
        verify(logAuditoriaRepository).findAllByOrderByDataHoraDesc(pageable);
    }

    @Test
    @DisplayName("registrar: deve salvar log com usuário autenticado do Spring Security")
    void registrar_comUsuarioAutenticado_deveSalvarComUsuario() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("usuario-teste");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        LogAuditoria logSalvo = new LogAuditoria("usuario-teste", "CRIAR", "Ativo", 1L, "Detalhe", null);
        when(logAuditoriaRepository.save(any(LogAuditoria.class))).thenReturn(logSalvo);

        LogAuditoria resultado = logAuditoriaService.registrar("CRIAR", "Ativo", 1L, "Detalhe");

        assertThat(resultado).isNotNull();
        verify(logAuditoriaRepository).save(argThat(log -> 
            "usuario-teste".equals(log.getUsuario()) &&
            "CRIAR".equals(log.getAcao()) &&
            "Ativo".equals(log.getEntidade()) &&
            Long.valueOf(1L).equals(log.getEntidadeId()) &&
            "Detalhe".equals(log.getDetalhes()) &&
            log.getDataHora() != null
        ));
    }

    @Test
    @DisplayName("registrar: deve salvar log com 'sistema' se não houver usuário autenticado")
    void registrar_semUsuarioAutenticado_deveSalvarComSistema() {
        SecurityContextHolder.clearContext();

        LogAuditoria logSalvo = new LogAuditoria("sistema", "CRIAR", "Ativo", 1L, "Detalhe", null);
        when(logAuditoriaRepository.save(any(LogAuditoria.class))).thenReturn(logSalvo);

        LogAuditoria resultado = logAuditoriaService.registrar("CRIAR", "Ativo", 1L, "Detalhe");

        assertThat(resultado).isNotNull();
        verify(logAuditoriaRepository).save(argThat(log -> 
            "sistema".equals(log.getUsuario())
        ));
    }
}
