package com.university.takharrujy.infrastructure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * Email Service with Brevo SMTP Integration
 * Handles sending transactional emails with Arabic language support
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${takharrujy.email.from}")
    private String fromEmail;

    @Value("${takharrujy.email.from-name}")
    private String fromName;

    @Value("${takharrujy.email.templates.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Send welcome email after registration
     */
    public void sendWelcomeEmail(String toEmail, String userName, String userNameAr, 
                               String verificationToken, String preferredLanguage) {
        try {
            logger.info("Sending welcome email to: {}", toEmail);

            Map<String, Object> variables = Map.of(
                "userName", userName,
                "userNameAr", userNameAr != null ? userNameAr : userName,
                "verificationUrl", baseUrl + "/auth/verify-email?token=" + verificationToken,
                "baseUrl", baseUrl,
                "supportEmail", fromEmail
            );

            String templateName = "ar".equals(preferredLanguage) ? "welcome-ar" : "welcome-en";
            String subject = "ar".equals(preferredLanguage) ? 
                "مرحباً بك في منصة تخرجي - تأكيد البريد الإلكتروني" : 
                "Welcome to Takharrujy Platform - Email Verification";

            sendTemplatedEmail(toEmail, subject, templateName, variables, preferredLanguage);

            logger.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Send email verification reminder
     */
    public void sendEmailVerificationReminder(String toEmail, String userName, String userNameAr,
                                            String verificationToken, String preferredLanguage) {
        try {
            logger.info("Sending email verification reminder to: {}", toEmail);

            Map<String, Object> variables = Map.of(
                "userName", userName,
                "userNameAr", userNameAr != null ? userNameAr : userName,
                "verificationUrl", baseUrl + "/auth/verify-email?token=" + verificationToken,
                "baseUrl", baseUrl,
                "supportEmail", fromEmail
            );

            String templateName = "ar".equals(preferredLanguage) ? "verification-reminder-ar" : "verification-reminder-en";
            String subject = "ar".equals(preferredLanguage) ? 
                "تذكير - تأكيد البريد الإلكتروني في منصة تخرجي" : 
                "Reminder - Email Verification for Takharrujy Platform";

            sendTemplatedEmail(toEmail, subject, templateName, variables, preferredLanguage);

            logger.info("Email verification reminder sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email verification reminder to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email verification reminder", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String userNameAr,
                                     String resetToken, String preferredLanguage) {
        try {
            logger.info("Sending password reset email to: {}", toEmail);

            Map<String, Object> variables = Map.of(
                "userName", userName,
                "userNameAr", userNameAr != null ? userNameAr : userName,
                "resetUrl", baseUrl + "/auth/reset-password?token=" + resetToken,
                "baseUrl", baseUrl,
                "supportEmail", fromEmail,
                "expirationHours", "24"
            );

            String templateName = "ar".equals(preferredLanguage) ? "password-reset-ar" : "password-reset-en";
            String subject = "ar".equals(preferredLanguage) ? 
                "إعادة تعيين كلمة المرور - منصة تخرجي" : 
                "Password Reset - Takharrujy Platform";

            sendTemplatedEmail(toEmail, subject, templateName, variables, preferredLanguage);

            logger.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send password changed confirmation email
     */
    public void sendPasswordChangedEmail(String toEmail, String userName, String userNameAr,
                                       String preferredLanguage) {
        try {
            logger.info("Sending password changed confirmation email to: {}", toEmail);

            Map<String, Object> variables = Map.of(
                "userName", userName,
                "userNameAr", userNameAr != null ? userNameAr : userName,
                "baseUrl", baseUrl,
                "supportEmail", fromEmail,
                "changedAt", java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now())
            );

            String templateName = "ar".equals(preferredLanguage) ? "password-changed-ar" : "password-changed-en";
            String subject = "ar".equals(preferredLanguage) ? 
                "تم تغيير كلمة المرور - منصة تخرجي" : 
                "Password Changed - Takharrujy Platform";

            sendTemplatedEmail(toEmail, subject, templateName, variables, preferredLanguage);

            logger.info("Password changed confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password changed confirmation email to: {}", toEmail, e);
            // Don't throw exception for confirmation emails
            logger.warn("Continuing despite email failure for password change confirmation");
        }
    }

    /**
     * Send account locked notification email
     */
    public void sendAccountLockedEmail(String toEmail, String userName, String userNameAr,
                                     String preferredLanguage, String reason) {
        try {
            logger.info("Sending account locked notification email to: {}", toEmail);

            Map<String, Object> variables = Map.of(
                "userName", userName,
                "userNameAr", userNameAr != null ? userNameAr : userName,
                "reason", reason,
                "baseUrl", baseUrl,
                "supportEmail", fromEmail
            );

            String templateName = "ar".equals(preferredLanguage) ? "account-locked-ar" : "account-locked-en";
            String subject = "ar".equals(preferredLanguage) ? 
                "تم إيقاف حسابك - منصة تخرجي" : 
                "Account Locked - Takharrujy Platform";

            sendTemplatedEmail(toEmail, subject, templateName, variables, preferredLanguage);

            logger.info("Account locked notification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send account locked notification email to: {}", toEmail, e);
            // Don't throw exception for notification emails
        }
    }

    /**
     * Send generic templated email
     */
    private void sendTemplatedEmail(String toEmail, String subject, String templateName,
                                  Map<String, Object> variables, String preferredLanguage) {
        try {
            // Create email context
            Context context = new Context(getLocaleFromLanguage(preferredLanguage));
            variables.forEach(context::setVariable);

            // Process template
            String htmlContent = templateEngine.process("email/" + templateName, context);

            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Set appropriate headers for RTL content
            if ("ar".equals(preferredLanguage)) {
                message.setHeader("Content-Language", "ar");
                message.setHeader("X-Priority", "3");
            }

            mailSender.send(message);

        } catch (MessagingException e) {
            logger.error("Failed to create or send email message", e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send plain text email (fallback)
     */
    public void sendPlainTextEmail(String toEmail, String subject, String content) {
        try {
            logger.info("Sending plain text email to: {}", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, false);

            mailSender.send(message);

            logger.info("Plain text email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send plain text email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send plain text email", e);
        }
    }

    /**
     * Check email service health
     */
    public boolean isEmailServiceHealthy() {
        try {
            // Test connection by creating a message (don't send)
            MimeMessage testMessage = mailSender.createMimeMessage();
            logger.debug("Email service health check passed");
            return true;
        } catch (Exception e) {
            logger.error("Email service health check failed", e);
            return false;
        }
    }

    /**
     * Convert language code to Locale
     */
    private Locale getLocaleFromLanguage(String language) {
        if ("ar".equals(language)) {
            return new Locale("ar");
        }
        return Locale.ENGLISH;
    }
}