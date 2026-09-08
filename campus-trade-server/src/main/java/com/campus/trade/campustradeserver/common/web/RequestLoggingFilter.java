package com.campus.trade.campustradeserver.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long started = System.nanoTime();
        String previous = MDC.get("requestId");
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        boolean failed = false;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failed = true;
            throw exception;
        } finally {
            // Only log the matched route template, never raw paths, query strings or bodies.
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            log.info("request method={} route={} status={} durationMs={} unhandled={}",
                    request.getMethod(), route == null ? "unmatched" : route,
                    response.getStatus(), (System.nanoTime() - started) / 1_000_000, failed);
            if (previous == null) {
                MDC.remove("requestId");
            } else {
                MDC.put("requestId", previous);
            }
        }
    }
}
