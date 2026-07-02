package com.legistrack.app.repository;

import com.legistrack.app.model.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @EntityGraph(attributePaths = "bill")
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.processed = true WHERE n.userId = :userId AND n.processed = false")
    int markAllProcessedForUser(@Param("userId") UUID userId);
}
