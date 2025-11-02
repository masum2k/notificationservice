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

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationJobRepository notificationJobRepository;

    @KafkaListener(topics = "todo-events", groupId = "notification-group")
    public void handleTodoEvent(TodoNotificationEvent event) {
        log.info("Kafka'dan yeni olay (event) alındı: {}", event);

        try {
            Instant deadlineInstant = Instant.ofEpochMilli(event.deadline());
            Instant notificationTimeInstant = deadlineInstant.minus(30, ChronoUnit.SECONDS);

            if (notificationTimeInstant.isAfter(Instant.now())) {
                NotificationJob job = new NotificationJob();
                job.setTodoId(event.todoId());
                job.setMessage("'" + event.title() + "' başlıklı görevinizin son tarihi yaklaşıyor!");
                job.setStatus("PENDING");
                job.setNotificationTime(notificationTimeInstant.toEpochMilli());

                notificationJobRepository.save(job);
                log.info("Bildirim işi veritabanına kaydedildi. Gönderim zamanı: {}", notificationTimeInstant);

            } else {
                log.warn("Geçmiş tarihli bir görev (veya 1 günden az kalan) için bildirim oluşturulmadı. Todo ID: {}", event.todoId());
            }

        } catch (Exception e) {
            log.error("Kafka olayı işlenirken hata oluştu: {}", event, e);
        }
    }
}