package com.example.todoapp.dto;

public record TodoNotificationEvent(
        Long todoId,
        String title,
        Long deadline
) {
}