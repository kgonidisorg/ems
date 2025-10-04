package com.ecogrid.ems.gateway.filter;

import com.ecogrid.ems.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for JwtAuthenticationFilter
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private HttpHeaders headers;

    @Mock
    private ServerHttpRequest.Builder requestBuilder;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private GatewayFilter gatewayFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
        gatewayFilter = jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config());

        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(request.getHeaders()).thenReturn(headers);
        lenient().when(request.mutate()).thenReturn(requestBuilder);
        lenient().when(requestBuilder.headers(any())).thenReturn(requestBuilder);
        lenient().when(requestBuilder.build()).thenReturn(request);
        
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        lenient().when(exchange.mutate()).thenReturn(exchangeBuilder);
        lenient().when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
        lenient().when(exchangeBuilder.build()).thenReturn(exchange);
    }

    @Test
    void apply_PublicEndpoint_ShouldSkipAuthentication() {
        // Arrange
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/auth/login");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void apply_MissingAuthorizationHeader_ShouldReturnUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/devices");
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(response.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(response.getHeaders()).thenReturn(headers);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.bufferFactory().wrap(any(byte[].class))).thenReturn(mock(org.springframework.core.io.buffer.DataBuffer.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(headers).add(HttpHeaders.CONTENT_TYPE, "application/json");
        verifyNoInteractions(chain);
    }

    @Test
    void apply_InvalidAuthorizationHeader_ShouldReturnUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/devices");
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Invalid header");
        when(response.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(response.getHeaders()).thenReturn(headers);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.bufferFactory().wrap(any(byte[].class))).thenReturn(mock(org.springframework.core.io.buffer.DataBuffer.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(headers).add(HttpHeaders.CONTENT_TYPE, "application/json");
        verifyNoInteractions(chain);
    }

    @Test
    void apply_InvalidJwtToken_ShouldReturnUnauthorized() {
        // Arrange
        String invalidToken = "Bearer invalid.jwt.token";
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/devices");
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(invalidToken);
        when(jwtUtil.validateToken("invalid.jwt.token")).thenReturn(false);
        when(response.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(response.getHeaders()).thenReturn(headers);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.bufferFactory().wrap(any(byte[].class))).thenReturn(mock(org.springframework.core.io.buffer.DataBuffer.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).validateToken("invalid.jwt.token");
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(headers).add(HttpHeaders.CONTENT_TYPE, "application/json");
        verifyNoInteractions(chain);
    }

    @Test
    void apply_ValidJwtToken_ShouldAddUserHeadersAndContinue() {
        // Arrange
        String validToken = "Bearer valid.jwt.token";
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/devices");
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("test@example.com");
        when(jwtUtil.extractUserId("valid.jwt.token")).thenReturn(1L);
        when(jwtUtil.extractRole("valid.jwt.token")).thenReturn("ADMIN");
        when(chain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).validateToken("valid.jwt.token");
        verify(jwtUtil).extractUsername("valid.jwt.token");
        verify(jwtUtil).extractUserId("valid.jwt.token");
        verify(jwtUtil).extractRole("valid.jwt.token");
        verify(requestBuilder).headers(any());
        verify(chain).filter(any());
    }

    @Test
    void apply_JwtValidationException_ShouldReturnUnauthorized() {
        // Arrange
        String validToken = "Bearer valid.jwt.token";
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/devices");
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);
        when(jwtUtil.validateToken("valid.jwt.token")).thenThrow(new RuntimeException("JWT parsing error"));
        when(response.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(response.getHeaders()).thenReturn(headers);
        when(response.bufferFactory()).thenReturn(mock(org.springframework.core.io.buffer.DataBufferFactory.class));
        when(response.bufferFactory().wrap(any(byte[].class))).thenReturn(mock(org.springframework.core.io.buffer.DataBuffer.class));
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert      
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).validateToken("valid.jwt.token");
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(headers).add(HttpHeaders.CONTENT_TYPE, "application/json");
        verifyNoInteractions(chain);
    }
}