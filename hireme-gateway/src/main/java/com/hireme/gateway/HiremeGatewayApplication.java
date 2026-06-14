package com.hireme.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class HiremeGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HiremeGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("job-service", r -> r.path("/api/jobs/**")
                        .uri("lb://JobService"))
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("lb://AuthService"))
                .route("candidate-service", r -> r.path("/api/candidate/**")
                        .uri("lb://AuthService"))
                .route("resume-service", r -> r.path(
                                "/api/resumes/**",
                                "/api/contacts/**",
                                "/api/educations/**",
                                "/api/experiences/**",
                                "/api/skills/**",
                                "/api/languages/**",
                                "/api/templates/**"
                        )
                        .uri("lb://ResumeService"))
                .build();
    }
}
