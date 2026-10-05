package br.com.portifolio.portifolio_projetos_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient membroRestClient(RestClient.Builder builder, @Value("${app.membros.api.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}