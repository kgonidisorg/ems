package com.ecogrid.ems.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Request logging filter for API Gateway
 * Logs incoming requests for monitoring and debugging
 */
@Component
public class RequestLoggingFilter extends AbstractGatewayFilterFactory<RequestLoggingFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    public RequestLoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            long startTime = System.currentTimeMillis();
            
            logger.info("REQUEST: {} {} from {} - User-Agent: {}", 
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress(),
                request.getHeaders().getFirst("User-Agent"));

            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    logger.info("RESPONSE: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(),
                        request.getPath(),
                        exchange.getResponse().getStatusCode(),
                        duration);
                })
            );
        };
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        // Configuration properties can be added here if needed
    }
}