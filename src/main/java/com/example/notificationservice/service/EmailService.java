package com.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    /**
     * E-postayı MailHog'a gönderir.
     */
    public void sendEmail(String toEmail, String subject, String body) {

        try {
            log.info("E-posta MailHog'a gönderiliyor: Kime={}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();

            // MailHog için 'From' adresinin ne olduğu önemli değildir
            message.setFrom("noreply@todoapp-demo.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);

            log.info("E-posta başarıyla MailHog'a iletildi: Kime={}", toEmail);

        } catch (Exception e) {
            log.error("E-posta gönderimi başarısız: Kime={}", toEmail, e);
            throw new RuntimeException("E-posta gönderimi başarısız", e);
        }
    }
}