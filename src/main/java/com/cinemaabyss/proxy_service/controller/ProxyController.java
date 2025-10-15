package com.cinemaabyss.proxy_service.controller;

import com.cinemaabyss.proxy_service.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    @RequestMapping(value = "/api/movies/**",
            method = {RequestMethod.GET, RequestMethod.POST,
                    RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Object> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) Object requestBody) {
        log.info("proxy movies");
        return proxyService.proxyRequest(request, "movies", request.getRequestURI(), requestBody);
    }

    @GetMapping(value = "/health", produces = "application/json")
    public HealthStatusDto getEventsServiceHealth() {
        return new HealthStatusDto(true);
    }

    @RequestMapping(value = "/api/users/**",
            method = {RequestMethod.GET, RequestMethod.POST,
                    RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Object> proxyUsers(HttpServletRequest request,
                                             @RequestBody(required = false) Object requestBody) {
        log.info("proxy users");
        return proxyService.proxyRequest(request, "users", request.getRequestURI(), requestBody);
    }


}
