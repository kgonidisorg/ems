package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.entity.Alert;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.notification.email.from}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Send alert notification email
     */
    public void sendAlertNotification(Alert alert, String toEmail) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled");
            return;
        }
        
        logger.info("Sending alert notification email for alert ID: {} to: {}", alert.getId(), toEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(buildAlertSubject(alert));
            
            String htmlContent = buildAlertEmailContent(alert);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            logger.info("Alert notification email sent successfully for alert ID: {}", alert.getId());
            
        } catch (MessagingException e) {
            logger.error("Failed to send alert notification email for alert ID: {}", alert.getId(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
    
    /**
     * Send alert digest email
     */
    public void sendAlertDigest(String toEmail, String subject, String content) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled");
            return;
        }
        
        logger.info("Sending alert digest email to: {}", toEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            
            logger.info("Alert digest email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to send alert digest email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send digest email", e);
        }
    }
    
    /**
     * Build alert email subject
     */
    private String buildAlertSubject(Alert alert) {
        String severityText = getSeverityDisplayText(alert.getSeverity());
        return String.format("[EMS Alert - %s] %s", severityText, alert.getType());
    }
    
    /**
     * Build alert email content using Thymeleaf template
     */
    private String buildAlertEmailContent(Alert alert) {
        Context context = new Context();
        context.setVariable("alert", alert);
        context.setVariable("severityText", getSeverityDisplayText(alert.getSeverity()));
        context.setVariable("severityColor", getSeverityColor(alert.getSeverity()));
        context.setVariable("formattedDateTime", alert.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return templateEngine.process("alert-notification", context);
    }
    
    /**
     * Get display text for alert severity
     */
    private String getSeverityDisplayText(Alert.AlertSeverity severity) {
        return switch (severity) {
            case LOW -> "Low Priority";
            case MEDIUM -> "Medium Priority";
            case HIGH -> "High Priority";
            case CRITICAL -> "Critical";
        };
    }
    
    /**
     * Get color for alert severity
     */
    private String getSeverityColor(Alert.AlertSeverity severity) {
        return switch (severity) {
            case LOW -> "#28a745";      // Green
            case MEDIUM -> "#ffc107";   // Yellow
            case HIGH -> "#fd7e14";     // Orange
            case CRITICAL -> "#dc3545"; // Red
        };
    }
    
    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration() {
        try {
            logger.info("Testing email configuration...");
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(fromEmail); // Send test email to self
            helper.setSubject("EMS Notification Service - Test Email");
            helper.setText("This is a test email to verify the notification service email configuration.", false);
            
            mailSender.send(message);
            
            logger.info("Test email sent successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Email configuration test failed", e);
            return false;
        }
    }
}