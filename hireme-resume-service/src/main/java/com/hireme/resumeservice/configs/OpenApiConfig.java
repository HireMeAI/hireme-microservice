package com.hireme.resumeservice.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireMe Resume Service API")
                        .description("API for managing resumes, experiences, education, contacts, templates, skills and languages")
                        .version("1.0.0"))
                .servers(List.of(
                        // URL relative (= context-path) : résolue par Swagger UI sur l'origine
                        // qui sert la doc (la Gateway en agrégé, ou le service en direct).
                        new Server()
                                .url(contextPath.isBlank() ? "/" : contextPath)
                                .description("Via API Gateway / service")
                ));
    }
}
