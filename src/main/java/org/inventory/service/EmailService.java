package org.inventory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a simple plain text email (no attachment)
     */
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println(" Simple email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println(" Error sending simple email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send an email that may include an attachment.
     * If attachmentBytes and filename are null, it sends a normal email.
     */
    public void sendEmailWithAttachment(String toEmail,
                                        String subject,
                                        String body,
                                        byte[] attachmentBytes,
                                        String attachmentFilename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, attachmentBytes != null, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            if (attachmentBytes != null && attachmentFilename != null && !attachmentFilename.isBlank()) {
                ByteArrayResource resource = new ByteArrayResource(attachmentBytes);
                helper.addAttachment(attachmentFilename, resource);
            }

            mailSender.send(message);
            System.out.println("Email (with/without attachment) sent to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println(" Error sending email with attachment: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
