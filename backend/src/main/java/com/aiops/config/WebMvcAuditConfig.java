package com.aiops.config;

import com.aiops.interceptor.AuditRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcAuditConfig implements WebMvcConfigurer {

    private final AuditRequestInterceptor auditRequestInterceptor;

    public WebMvcAuditConfig(AuditRequestInterceptor auditRequestInterceptor) {
        this.auditRequestInterceptor = auditRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditRequestInterceptor)
                .addPathPatterns("/api/**");
    }
}
