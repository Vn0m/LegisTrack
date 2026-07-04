package com.legistrack.app.controller;

import com.legistrack.app.dto.MessageDto;
import com.legistrack.app.dto.NotificationsResponseDto;
import com.legistrack.app.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public NotificationsResponseDto getNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.getNotificationsForUser(CurrentUser.id(jwt));
    }

    @PutMapping("/{id}/read")
    public MessageDto markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID id) {
        notificationService.markRead(CurrentUser.id(jwt), id);
        return new MessageDto("Marked as read");
    }

    @PutMapping("/read-all")
    public MessageDto markAllRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllRead(CurrentUser.id(jwt));
        return new MessageDto("All notifications marked as read");
    }
}
