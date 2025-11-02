package com.example.notificationservice.repository;

import com.example.notificationservice.model.NotificationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJobRepository extends JpaRepository<NotificationJob, Long> {

    List<NotificationJob> findByStatusAndNotificationTimeLessThanEqual(String status, long nowEpochMilli);
}