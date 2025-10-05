package com.cinemaabyss.proxy_service.service;

import com.cinemaabyss.proxy_service.configuration.ProxyConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoutingStrategy {

    private final ProxyConfiguration proxyConfiguration;

    public String getTargetUrl(String resourceType) {
        if (!proxyConfiguration.isMigrationEnabled()) {
            return proxyConfiguration.getProperties().getCinemaabyssMainUrl();
        } else if("movies".equals(resourceType)) {
            return proxyConfiguration.getProperties().getCinemaabyssMoviesUrl();
        }
        return proxyConfiguration.getProperties().getCinemaabyssMainUrl();
    }
}
