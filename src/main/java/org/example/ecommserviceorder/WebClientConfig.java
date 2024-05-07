package org.example.ecommserviceorder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties
public class WebClientConfig {


    @Value("${gatewayserver.url}")
    private String gatewayserver_url;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(gatewayserver_url).build();
    }


}
