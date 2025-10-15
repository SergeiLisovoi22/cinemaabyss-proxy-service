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
        final var props = proxyConfiguration.getProperties();

        // Если миграция выключена — всё проксируем в монолит
        if (!proxyConfiguration.isMigrationEnabled()) {
            String url = props.getCinemaabyssMainUrl();
            log.debug("Migration disabled -> route '{}' to MAIN: {}", resourceType, url);
            return url;
        }

        // Миграция включена — роутим точечно
        if ("movies".equalsIgnoreCase(resourceType)) {
            String url = props.getCinemaabyssMoviesUrl();
            log.debug("Route '{}' to MOVIES: {}", resourceType, url);
            return url;
        }
        if ("events".equalsIgnoreCase(resourceType)) {
            String url = props.getCinemaabyssEventsUrl();
            log.debug("Route '{}' to EVENTS: {}", resourceType, url);
            return url;
        }

        // users и всё остальное по умолчанию — в монолит
        String url = props.getCinemaabyssMainUrl();
        log.debug("Route '{}' (default/users) to MAIN: {}", resourceType, url);
        return url;
    }
}
