package com.example.otpservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.internet.MimeMessage;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;
    private final String userBaseUrl;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        RestTemplate restTemplate,
                        @Value("${services.user.base-url}") String userBaseUrl,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
        this.userBaseUrl = userBaseUrl;
        this.fromEmail = fromEmail;
    }

    public String getUserEmail(BigInteger userId) {
        try {
            var resp = restTemplate.getForEntity(userBaseUrl + "/users/" + userId, java.util.Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) return null;
            Object email = resp.getBody().get("email");
            return email == null ? null : email.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // Dùng helper để set HTML và UTF-8
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // ⚠️ "true" nghĩa là gửi HTML

            mailSender.send(message);
            return true;

        } catch (Exception ex) {
            ex.printStackTrace(); // Ghi log ra console để dễ debug
            return false;
        }
    }
}
