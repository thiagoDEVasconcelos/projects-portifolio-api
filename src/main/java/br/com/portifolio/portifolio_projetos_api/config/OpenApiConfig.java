package br.com.portifolio.portifolio_projetos_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Portfólio de Projetos",
                version = "1.0",
                description = "Gerenciamento do ciclo de vida de projetos, equipe, orçamento e risco."),
        security = @SecurityRequirement(name = "basicAuth"))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic")
public class OpenApiConfig {
}