package br.ufpb.dsc.mercado.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração MVC para registrar interceptores.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final HtmxToastInterceptor htmxToastInterceptor;

    public WebConfig(HtmxToastInterceptor htmxToastInterceptor) {
        this.htmxToastInterceptor = htmxToastInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(htmxToastInterceptor);
    }
}
