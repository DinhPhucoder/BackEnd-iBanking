package com.example.otpservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final org.springframework.web.client.RestTemplate restTemplate;
    private final String userBaseUrl;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        org.springframework.web.client.RestTemplate restTemplate,
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
            // dùng SimpleMailMessage (text) để đơn giản, có thể nâng cấp MimeMessageHelper nếu cần HTML thực sự
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(html.replaceAll("<[^>]+>", "")); // fallback text
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}


