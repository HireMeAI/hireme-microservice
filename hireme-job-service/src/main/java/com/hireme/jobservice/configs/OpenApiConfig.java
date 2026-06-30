package com.hireme.jobservice.configs;

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

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireMe Job Service API")
                        .description("Gestion des offres d'emploi")
                        .version("1.0.0"))
                // URL relative (= context-path) : résolue par Swagger UI sur l'origine qui sert
                // la doc (la Gateway en agrégé, ou le service en direct).
                .servers(List.of(new Server()
                        .url(contextPath.isBlank() ? "/" : contextPath)
                        .description("Via API Gateway / service")));
    }
}
