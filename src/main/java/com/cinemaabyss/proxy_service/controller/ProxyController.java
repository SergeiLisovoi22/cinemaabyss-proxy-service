package com.cinemaabyss.proxy_service.controller;

import com.cinemaabyss.proxy_service.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
        log.info("proxy");
        return proxyService.proxyRequest(request, "movies", request.getRequestURI(), requestBody);
    }

}
