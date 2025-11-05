package com.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // <-- Bunu import et
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String toEmail, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);

        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        log.info("{} adresinden {} adresine e-posta gönderiliyor...", fromEmail, toEmail);

        try {
            javaMailSender.send(message);
            log.info("E-posta başarıyla gönderildi: Kime={}", toEmail);
        } catch (Exception e) {
            log.error("E-posta gönderimi başarısız: Kime={}", toEmail, e);
            throw new RuntimeException("E-posta gönderimi başarısız", e);
        }
    }
}