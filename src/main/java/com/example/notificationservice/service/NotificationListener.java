package com.example.notificationservice.service;

import com.example.notificationservice.model.NotificationJob;
import com.example.notificationservice.repository.NotificationJobRepository;
import com.example.todoapp.dto.TodoNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationJobRepository notificationJobRepository;
    private static final String STATUS_PENDING = "PENDING";

    @KafkaListener(topics = "todo-events", groupId = "notification-group")
    public void handleTodoEvent(TodoNotificationEvent event) {
        log.info("Kafka'dan yeni olay (event) alındı: {}", event);

        try {
            Optional<NotificationJob> existingJobOpt = notificationJobRepository
                    .findByTodoIdAndStatus(event.todoId(), STATUS_PENDING);

            if (event.deadline() == null) {
                existingJobOpt.ifPresent(job -> {
                    notificationJobRepository.delete(job);
                    log.info("Todo (ID: {}) için planlanmış bildirim iptal edildi (deadline kaldırıldı).", event.todoId());
                });
                return;
            }

            Instant deadlineInstant = Instant.ofEpochMilli(event.deadline());
            Instant notificationTimeInstant = deadlineInstant.minus(30, ChronoUnit.SECONDS);

            if (notificationTimeInstant.isAfter(Instant.now())) {

                // Mevcut işi al, eğer yoksa YENİ bir tane oluştur.
                NotificationJob job = existingJobOpt.orElseGet(NotificationJob::new);

                job.setTodoId(event.todoId());
                job.setMessage("'" + event.title() + "' başlıklı görevinizin son tarihi yaklaşıyor!");
                job.setStatus(STATUS_PENDING);
                job.setNotificationTime(notificationTimeInstant.toEpochMilli());
                job.setUserEmail(event.userEmail());

                notificationJobRepository.save(job);

                if (existingJobOpt.isPresent()) {
                    log.info("Bildirim işi güncellendi (ID: {}). Yeni gönderim zamanı: {}", job.getId(), notificationTimeInstant);
                } else {
                    log.info("Yeni bildirim işi veritabanına kaydedildi. Gönderim zamanı: {}", notificationTimeInstant);
                }

            } else {
                log.warn("Geçmiş tarihli bir görev (veya 30 saniyeden az kalan) için bildirim oluşturulmadı/güncellenmedi. Todo ID: {}", event.todoId());
                existingJobOpt.ifPresent(notificationJobRepository::delete);
            }

        } catch (Exception e) {
            log.error("Kafka olayı işlenirken hata oluştu: {}", event, e);
        }
    }
}