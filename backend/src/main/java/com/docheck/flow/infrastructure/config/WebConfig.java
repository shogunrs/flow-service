package com.docheck.flow.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.cors.origins:http://localhost:3000}")
    private String corsOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = corsOrigins.split(",");
        boolean wildcard = corsOrigins.contains("*");

        var apiMapping = registry.addMapping("/api/**")
                .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
        var actMapping = registry.addMapping("/actuator/**")
                .allowedMethods("GET","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);

        if (wildcard) {
            apiMapping.allowedOriginPatterns("*");
            actMapping.allowedOriginPatterns("*");
        } else {
            apiMapping.allowedOrigins(origins);
            actMapping.allowedOrigins(origins);
        }
    }
}
