package com.example.notificationservice.service;

import com.example.notificationservice.model.NotificationJob;
import com.example.notificationservice.repository.NotificationJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationJobRepository notificationJobRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 10000)
    public void processPendingNotifications() {

        log.info("Zamanlanmış görev çalıştı. Gönderilecek bildirimler kontrol ediliyor...");

        List<NotificationJob> jobsToSend = notificationJobRepository
                .findByStatusAndNotificationTimeLessThanEqual("PENDING", Instant.now().toEpochMilli());

        if (jobsToSend.isEmpty()) {
            log.info("Gönderilecek yeni bildirim bulunamadı.");
            return;
        }

        log.info("{} adet gönderilecek bildirim bulundu. İşleniyor...", jobsToSend.size());

        for (NotificationJob job : jobsToSend) {
            try {
                String userEmail = "user@example.com";

                emailService.sendEmail(
                        userEmail,
                        "Todo Görevi Hatırlatması!",
                        job.getMessage()
                );

                job.setStatus("SENT");

            } catch (Exception e) {
                log.error("E-posta gönderilirken hata oluştu (iş ID: {}): {}", job.getId(), e.getMessage());
                job.setStatus("FAILED");
            }

            notificationJobRepository.save(job);
        }
    }
}