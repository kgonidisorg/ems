package com.ecogrid.ems.analytics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration
 * Enables automatic auditing of JPA entities with @CreatedDate, @LastModifiedDate, etc.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}