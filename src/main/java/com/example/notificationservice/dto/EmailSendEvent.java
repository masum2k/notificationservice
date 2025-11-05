package com.example.notificationservice.dto;

public record EmailSendEvent(
        String toEmail,
        String subject,
        String body
) {
}