package com.example.notificationservice.service;

import com.example.notificationservice.dto.EmailSendEvent; // Yeni DTO'yu import et
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;

    @KafkaListener(topics = "email-send-queue", groupId = "notification-group")
    public void handleEmailSendEvent(EmailSendEvent event) {
        log.info("Yeni e-posta gönderme komutu alındı: Kime={}", event.toEmail());

        try {
            emailService.sendEmail(
                    event.toEmail(),
                    event.subject(),
                    event.body()
            );
            log.info("E-posta gönderme komutu başarıyla işlendi: Kime={}", event.toEmail());

        } catch (Exception e) {
            log.error("E-posta gönderme komutu işlenirken hata oluştu: {}", event, e);
        }
    }
}