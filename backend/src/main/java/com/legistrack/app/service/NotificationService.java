package com.legistrack.app.service;

import com.legistrack.app.dto.NotificationDto;
import com.legistrack.app.dto.NotificationsResponseDto;
import com.legistrack.app.exception.NotFoundException;
import com.legistrack.app.model.Bill;
import com.legistrack.app.model.Notification;
import com.legistrack.app.model.SavedBill;
import com.legistrack.app.repository.NotificationRepository;
import com.legistrack.app.repository.SavedBillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SavedBillRepository savedBillRepository;
    private final AwsSqsService awsSqsService;

    public NotificationService(NotificationRepository notificationRepository,
                               SavedBillRepository savedBillRepository,
                               AwsSqsService awsSqsService) {
        this.notificationRepository = notificationRepository;
        this.savedBillRepository = savedBillRepository;
        this.awsSqsService = awsSqsService;
    }

    public NotificationsResponseDto getNotificationsForUser(UUID userId) {
        List<Notification> notifications = notificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
        long unreadCount = notifications.stream().filter(n -> !n.isProcessed()).count();
        return new NotificationsResponseDto(
            notifications.stream().map(NotificationDto::from).toList(),
            unreadCount);
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .filter(n -> userId.equals(n.getUserId()))
            .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.setProcessed(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllProcessedForUser(userId);
    }

    @Transactional
    public void checkAndNotifyBillStatusChanges(Bill bill, String previousStatus) {
        if (bill.getStatus() == null || bill.getStatus().equals(previousStatus)) {
            return;
        }

        List<SavedBill> savedBillsByUsers = savedBillRepository.findByBill(bill);

        for (SavedBill savedBill : savedBillsByUsers) {
            try {
                Notification notification = new Notification(
                    savedBill.getUserId(),
                    bill,
                    Notification.NotificationType.BILL_STATUS_CHANGED,
                    previousStatus,
                    bill.getStatus()
                );
                notificationRepository.save(notification);

                awsSqsService.publishBillStatusChange(
                    savedBill.getUserId().toString(),
                    bill.getBasePrintNoStr(),
                    bill.getTitle(),
                    previousStatus,
                    bill.getStatus()
                );

                logger.info("Created and published notification for user {} about bill {}",
                    savedBill.getUserId(), bill.getBasePrintNoStr());
            } catch (Exception e) {
                logger.error("Error creating notification for user {}: {}",
                    savedBill.getUserId(), e.getMessage(), e);
            }
        }
    }
}
