package com.university.takharrujy.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Test configuration for email service that bypasses actual SMTP sending
 */
@Configuration
@Profile("test")
public class TestEmailConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestEmailConfig.class);

    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MockMimeMessage();
            }

            @Override
            public MimeMessage createMimeMessage(java.io.InputStream contentStream) throws org.springframework.mail.MailException {
                return new MockMimeMessage();
            }

            @Override
            public void send(MimeMessage mimeMessage) throws org.springframework.mail.MailException {
                logger.info("Mock email sent - bypassing actual SMTP");
                // Do nothing - just log that email would be sent
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws org.springframework.mail.MailException {
                logger.info("Mock emails sent - bypassing actual SMTP, count: {}", mimeMessages.length);
                // Do nothing - just log that emails would be sent
            }

            @Override
            public void send(org.springframework.mail.SimpleMailMessage simpleMessage) throws org.springframework.mail.MailException {
                logger.info("Mock simple email sent - bypassing actual SMTP");
                // Do nothing - just log that email would be sent
            }

            @Override
            public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) throws org.springframework.mail.MailException {
                logger.info("Mock simple emails sent - bypassing actual SMTP, count: {}", simpleMessages.length);
                // Do nothing - just log that emails would be sent
            }
        };
    }

    /**
     * Mock MimeMessage implementation for testing
     */
    private static class MockMimeMessage extends MimeMessage {
        public MockMimeMessage() {
            super((jakarta.mail.Session) null);
        }

        @Override
        public void setFrom(jakarta.mail.Address address) throws MessagingException {
            // Mock implementation
        }

        @Override
        public void setRecipients(jakarta.mail.Message.RecipientType type, jakarta.mail.Address[] addresses) throws MessagingException {
            // Mock implementation
        }

        @Override
        public void setSubject(String subject) throws MessagingException {
            // Mock implementation
        }

        @Override
        public void setContent(Object o, String type) throws MessagingException {
            // Mock implementation
        }

        @Override
        public void saveChanges() throws MessagingException {
            // Mock implementation
        }
    }
}