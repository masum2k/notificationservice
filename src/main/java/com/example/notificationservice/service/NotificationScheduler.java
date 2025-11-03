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

    @Scheduled(fixedRate = 10000) //10 saniye
    public void processPendingNotifications() {

        log.info("Gönderilecek bildirimler kontrol ediliyor...");

        List<NotificationJob> jobsToSend = notificationJobRepository
                .findByStatusAndNotificationTimeLessThanEqual("PENDING", Instant.now().toEpochMilli());

        if (jobsToSend.isEmpty()) {
            log.info("Gönderilecek yeni bildirim bulunamadı.");
            return;
        }

        log.info("{} adet gönderilecek bildirim bulundu. İşleniyor...", jobsToSend.size());

        for (NotificationJob job : jobsToSend) {
            try {
                String userEmail = job.getUserEmail();

                if (userEmail == null || userEmail.isBlank()) {
                    log.error("E-posta adresi boş olduğu için bildirim gönderilemedi (iş ID: {})", job.getId());
                    job.setStatus("FAILED");
                    notificationJobRepository.save(job);
                    continue;
                }

                emailService.sendEmail(
                        userEmail,  //send to this email
                        "Todo Görevi Hatırlatması!",   //subject of email is this
                        job.getMessage()   //text of mail
                );

                job.setStatus("SENT");   //PENDING -> SENT

            } catch (Exception e) {
                log.error("E-posta gönderilirken hata oluştu (iş ID: {}): {}", job.getId(), e.getMessage());
                job.setStatus("FAILED");
            }

            notificationJobRepository.save(job);
        }
    }
}