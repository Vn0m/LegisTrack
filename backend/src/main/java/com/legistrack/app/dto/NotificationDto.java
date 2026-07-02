package com.legistrack.app.dto;

import com.legistrack.app.model.Notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    String basePrintNoStr,
    String billTitle,
    String oldStatus,
    String newStatus,
    OffsetDateTime createdAt,
    boolean processed
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getBill().getBasePrintNoStr(), n.getBill().getTitle(),
            n.getOldStatus(), n.getNewStatus(), n.getCreatedAt(), n.isProcessed());
    }
}
