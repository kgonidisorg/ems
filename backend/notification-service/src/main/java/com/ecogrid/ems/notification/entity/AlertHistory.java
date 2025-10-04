package com.ecogrid.ems.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_history")
public class AlertHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "alert_id")
    private Long alertId;
    
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeliveryStatus status;
    
    @Column(name = "recipient")
    private String recipient; // Email address or WebSocket session ID
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;
    
    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public AlertHistory() {}
    
    public AlertHistory(Long alertId, Long userId, NotificationType notificationType, String recipient) {
        this.alertId = alertId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.recipient = recipient;
        this.status = DeliveryStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAlertId() {
        return alertId;
    }
    
    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public DeliveryStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public enum NotificationType {
        EMAIL, WEBSOCKET, SMS
    }
    
    public enum DeliveryStatus {
        PENDING, DELIVERED, FAILED, RETRYING
    }
}