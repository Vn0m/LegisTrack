package com.legistrack.app.service;

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
import java.util.Optional;
import java.util.UUID;

@Service
@SuppressWarnings("null")
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

    public List<Notification> getUnprocessedNotifications() {
        return notificationRepository.findByProcessedFalseOrderByCreatedAtAsc();
    }

    @Transactional
    public void markAsProcessed(UUID notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        notification.ifPresent(n -> {
            n.setProcessed(true);
            notificationRepository.save(n);
        });
    }
}
