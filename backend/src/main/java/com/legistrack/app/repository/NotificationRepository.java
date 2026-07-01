package com.legistrack.app.repository;

import com.legistrack.app.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByProcessedFalseOrderByCreatedAtAsc();
    List<Notification> findByUserIdAndProcessedFalse(UUID userId);
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}
