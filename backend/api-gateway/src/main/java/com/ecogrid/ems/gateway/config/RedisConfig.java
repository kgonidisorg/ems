package com.ecogrid.ems.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for API Gateway
 * Makes Redis optional for development
 */
@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Bean
    @ConditionalOnProperty(name = "spring.redis.host", matchIfMissing = false)
    public RedisConnectionFactory redisConnectionFactory() {
        logger.info("=== API GATEWAY REDIS CONFIGURATION DEBUG ===");
        logger.info("Redis Host: {}", redisHost);
        logger.info("Redis Port: {}", redisPort);
        logger.info("Active Profile: {}", System.getProperty("spring.profiles.active"));
        logger.info("REDIS_HOST env var: {}", System.getenv("REDIS_HOST"));
        logger.info("REDIS_PORT env var: {}", System.getenv("REDIS_PORT"));
        
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        
        logger.info("Creating LettuceConnectionFactory with host: {}:{}", redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.host", matchIfMissing = false)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return template;
    }
}