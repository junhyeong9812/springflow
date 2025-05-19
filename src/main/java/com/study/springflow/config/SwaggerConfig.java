package com.study.springflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger(OpenAPI) 설정 클래스
 * - API 문서화 및 테스트 인터페이스 제공
 * - JWT 토큰 인증 설정 포함
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 설정
     * - API 기본 정보 설정
     * - JWT 인증 설정
     * - 서버 정보 설정
     */
    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 보안 요구사항 설정
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // 기본 서버 설정
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local 개발 서버");

        return new OpenAPI()
                // API 기본 정보
                .info(new Info()
                        .title("SpringFlow API")
                        .description("Spring Framework의 요청 처리 흐름과 보안 학습을 위한 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("SpringFlow")
                                .email("example@springflow.com")
                                .url("https://github.com/yourusername/springflow"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 서버 정보
                .servers(Arrays.asList(localServer))
                // 보안 스키마 및 요구사항 설정
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}