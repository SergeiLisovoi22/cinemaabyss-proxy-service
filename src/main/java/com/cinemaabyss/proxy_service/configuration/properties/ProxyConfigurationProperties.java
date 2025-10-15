package com.cinemaabyss.proxy_service.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proxy")
@Getter
@Setter
public class ProxyConfigurationProperties {

    private String cinemaabyssMainUrl;
    private String cinemaabyssMoviesUrl;
    private String cinemaabyssEventsUrl;
    private boolean cinemaabyssProxyEnabled;
}
