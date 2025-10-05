package com.cinemaabyss.proxy_service.configuration;

import com.cinemaabyss.proxy_service.configuration.properties.ProxyConfigurationProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Getter
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(ProxyConfigurationProperties.class)
public class ProxyConfiguration {

    private final ProxyConfigurationProperties properties;

    public boolean isMigrationEnabled() {
        return properties.isCinemaabyssProxyEnabled();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}
