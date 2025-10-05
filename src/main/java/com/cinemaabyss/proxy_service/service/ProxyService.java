package com.cinemaabyss.proxy_service.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestClient restClient;
    private final RoutingStrategy routingStrategy;

    /**
     * Проксирует запрос к целевому сервису
     */
    public ResponseEntity<Object> proxyRequest(
            HttpServletRequest request,
            String resourceType,
            String path,
            Object body) {

        String targetUrl = routingStrategy.getTargetUrl(resourceType);
        String fullUrl = buildFullUrl(targetUrl, path, request.getQueryString());

        log.info("Proxying {} request to: {}", request.getMethod(), fullUrl);

        try {
            HttpHeaders headers = extractHeaders(request);

            ResponseEntity<Object> response = switch (request.getMethod().toUpperCase()) {
                case "GET" -> executeGet(fullUrl, headers);
                case "POST" -> executePost(fullUrl, headers, body);
                case "PUT" -> executePut(fullUrl, headers, body);
                case "DELETE" -> executeDelete(fullUrl, headers);
                case "PATCH" -> executePatch(fullUrl, headers, body);
                default -> ResponseEntity
                        .status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body("Method not supported");
            };

            log.info("Response from target service: status={}", response.getStatusCode());
            return response;

        } catch (Exception e) {
            log.error("Error proxying request to {}: {}", fullUrl, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Error communicating with backend service: " + e.getMessage());
        }
    }

    private String buildFullUrl(String baseUrl, String path, String queryString) {
        String url = baseUrl + path;
        if (queryString != null && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        return url;
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Пропускаем заголовки, которые не должны проксироваться
            if (shouldSkipHeader(headerName)) {
                continue;
            }
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            headers.addAll(headerName, headerValues);
        }

        return headers;
    }

    private boolean shouldSkipHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("host") ||
                lowerName.equals("content-length") ||
                lowerName.equals("transfer-encoding");
    }

    private ResponseEntity<Object> executeGet(String url, HttpHeaders headers) {
        return restClient.get()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .toEntity(Object.class);
    }

    private ResponseEntity<Object> executePost(String url, HttpHeaders headers, Object body) {
        return restClient.post()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .body(body != null ? body : "")
                .retrieve()
                .toEntity(Object.class);
    }

    private ResponseEntity<Object> executePut(String url, HttpHeaders headers, Object body) {
        return restClient.put()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .body(body != null ? body : "")
                .retrieve()
                .toEntity(Object.class);
    }

    private ResponseEntity<Object> executeDelete(String url, HttpHeaders headers) {
        return restClient.delete()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .toEntity(Object.class);
    }

    private ResponseEntity<Object> executePatch(String url, HttpHeaders headers, Object body) {
        return restClient.patch()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .body(body != null ? body : "")
                .retrieve()
                .toEntity(Object.class);
    }
}

