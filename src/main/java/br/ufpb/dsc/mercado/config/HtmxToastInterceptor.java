package br.ufpb.dsc.mercado.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor para capturar "toastMensagem" do Model do Spring MVC
 * e enviar como um cabeçalho HTTP "HX-Trigger" para o HTMX exibir o toast na tela.
 */
@Component
public class HtmxToastInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && modelAndView.getModelMap().containsAttribute("toastMensagem")) {
            String mensagem = (String) modelAndView.getModelMap().get("toastMensagem");
            if (mensagem != null && !mensagem.isEmpty()) {
                // Remove caracteres especiais para evitar quebra de JSON simples
                String sanitized = mensagem.replace("\"", "\\\"");
                String jsonHeader = String.format("{\"showToast\":\"%s\",\"toastType\":\"success\"}", sanitized);
                response.setHeader("HX-Trigger", jsonHeader);
            }
        }
    }
}
