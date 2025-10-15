package com.cinemaabyss.proxy_service.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestClient restClient;
    private final RoutingStrategy routingStrategy;

    public ResponseEntity<Object> proxyRequest(HttpServletRequest request,
                                               String resourceType,
                                               String path,
                                               Object body) {
        final String baseUrl = routingStrategy.getTargetUrl(resourceType);
        final String fullUrl = buildFullUrl(baseUrl, path, request.getQueryString());
        log.info("Proxy {} {} -> {}", request.getMethod(), path, fullUrl);

        try {
            HttpHeaders fwdHeaders = extractHeaders(request); // входящие заголовки

            // выполняем запрос вверх и получаем ТЕКСТ
            ResponseEntity<String> upstream = switch (request.getMethod().toUpperCase()) {
                case "GET"    -> restClient.get().uri(fullUrl).headers(h -> h.addAll(fwdHeaders)).retrieve().toEntity(String.class);
                case "POST"   -> restClient.post().uri(fullUrl).headers(h -> h.addAll(fwdHeaders))
                        .body(body != null ? body : "").retrieve().toEntity(String.class);
                case "PUT"    -> restClient.put().uri(fullUrl).headers(h -> h.addAll(fwdHeaders))
                        .body(body != null ? body : "").retrieve().toEntity(String.class);
                case "DELETE" -> restClient.delete().uri(fullUrl).headers(h -> h.addAll(fwdHeaders)).retrieve().toEntity(String.class);
                case "PATCH"  -> restClient.patch().uri(fullUrl).headers(h -> h.addAll(fwdHeaders))
                        .body(body != null ? body : "").retrieve().toEntity(String.class);
                default -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Method not supported");
            };

            // готовим ответ вниз: переносим безопасные заголовки + тело как пришло
            HttpHeaders downHeaders = filterDownstreamHeaders(upstream.getHeaders());
            // гарантируем правильный content-type (если не пришёл) — тесты ждут JSON
            downHeaders.putIfAbsent(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));

            return ResponseEntity.status(upstream.getStatusCode())
                    .headers(downHeaders)
                    .body(upstream.getBody());

        } catch (Exception e) {
            log.error("Proxy error to {}: {}", fullUrl, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /* helpers */

    private String buildFullUrl(String baseUrl, String path, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append(stripTrailingSlash(baseUrl)).append(path);
        if (query != null && !query.isEmpty()) sb.append('?').append(query);
        return sb.toString();
    }

    private String stripTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (shouldSkipInboundHeader(name)) continue;
            headers.addAll(name, Collections.list(request.getHeaders(name)));
        }
        // уменьшаем шанс проблем с компрессией/соединением
        headers.remove(HttpHeaders.ACCEPT_ENCODING);
        headers.set(HttpHeaders.CONNECTION, "close");
        return headers;
    }

    private boolean shouldSkipInboundHeader(String name) {
        String n = name.toLowerCase();
        return n.equals("host") || n.equals("content-length") || n.equals("transfer-encoding");
    }

    private HttpHeaders filterDownstreamHeaders(HttpHeaders upstream) {
        HttpHeaders h = new HttpHeaders();
        upstream.forEach((k, v) -> {
            String n = k.toLowerCase();
            if (n.equals("content-length") || n.equals("transfer-encoding") || n.equals("connection")) return;
            h.put(k, v);
        });
        h.set(HttpHeaders.CONNECTION, "close");
        return h;
    }
}
