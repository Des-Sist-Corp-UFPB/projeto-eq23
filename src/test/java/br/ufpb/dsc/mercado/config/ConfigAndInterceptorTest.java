package br.ufpb.dsc.mercado.config;

import br.ufpb.dsc.mercado.service.EmailService;
import br.ufpb.dsc.mercado.service.MockEmailService;
import br.ufpb.dsc.mercado.service.ResendEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigAndInterceptorTest {

    @Test
    @DisplayName("HtmxToastInterceptor — Deve adicionar header HX-Trigger se toastMensagem existir")
    void htmxToastInterceptor_ComMensagem_DeveAdicionarHeader() throws Exception {
        HtmxToastInterceptor interceptor = new HtmxToastInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("toastMensagem", "Salvo com sucesso!");

        interceptor.postHandle(request, response, new Object(), modelAndView);

        verify(response, times(1)).setHeader(
                eq("HX-Trigger"),
                eq("{\"showToast\":\"Salvo com sucesso!\",\"toastType\":\"success\"}")
        );
    }

    @Test
    @DisplayName("HtmxToastInterceptor — Não deve fazer nada se toastMensagem não existir")
    void htmxToastInterceptor_SemMensagem_NaoDeveAdicionarHeader() throws Exception {
        HtmxToastInterceptor interceptor = new HtmxToastInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        interceptor.postHandle(request, response, new Object(), new ModelAndView());

        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    @DisplayName("GlobalModelAttributes — Deve retornar request URI")
    void globalModelAttributes_DeveRetornarRequestURI() {
        GlobalModelAttributes advice = new GlobalModelAttributes();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/ativos/1");

        String uri = advice.requestURI(request);
        assertEquals("/ativos/1", uri);
    }

    @Test
    @DisplayName("EmailConfig — Deve injetar condicionalmente MockEmailService ou ResendEmailService")
    void emailConfig_DeveInjetarCorretamente() {
        EmailConfig config = new EmailConfig();
        ReflectionTestUtils.setField(config, "apiUrl", "http://test.com");
        ReflectionTestUtils.setField(config, "from", "from@test.com");
        ReflectionTestUtils.setField(config, "to", "to@test.com");

        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);
        when(builder.build()).thenReturn(restClient);

        // Sem API Key -> MockEmailService
        ReflectionTestUtils.setField(config, "apiKey", "");
        EmailService serviceMock = config.emailService(builder);
        assertTrue(serviceMock instanceof MockEmailService);

        // Com API Key -> ResendEmailService
        ReflectionTestUtils.setField(config, "apiKey", "test-key");
        EmailService serviceReal = config.emailService(builder);
        assertTrue(serviceReal instanceof ResendEmailService);
    }

    @Test
    @DisplayName("WebConfig — Deve registrar HtmxToastInterceptor")
    void webConfig_DeveRegistrarInterceptor() {
        HtmxToastInterceptor interceptor = new HtmxToastInterceptor();
        WebConfig webConfig = new WebConfig(interceptor);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);

        webConfig.addInterceptors(registry);
        verify(registry, times(1)).addInterceptor(interceptor);
    }
}
