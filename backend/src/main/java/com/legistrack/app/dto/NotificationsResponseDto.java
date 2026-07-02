package com.legistrack.app.dto;

import java.util.List;

public record NotificationsResponseDto(List<NotificationDto> notifications, long unreadCount) {}
