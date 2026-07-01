package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.legistrack.app.model.Notification;
import com.legistrack.app.repository.NotificationRepository;
import com.legistrack.app.service.SupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final SupabaseService supabaseService;
    private final ObjectMapper objectMapper;

    public NotificationController(NotificationRepository notificationRepository,
                                  SupabaseService supabaseService,
                                  ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.supabaseService = supabaseService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> getNotifications(@RequestHeader("Authorization") String authToken) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        List<Notification> notifications = notificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
        long unreadCount = notifications.stream().filter(n -> !n.isProcessed()).count();

        ArrayNode arr = objectMapper.createArrayNode();
        for (Notification n : notifications) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", n.getId() != null ? n.getId().toString() : "");
            node.put("basePrintNoStr", n.getBill().getBasePrintNoStr());
            node.put("billTitle", n.getBill().getTitle());
            node.put("oldStatus", n.getOldStatus());
            node.put("newStatus", n.getNewStatus());
            node.put("createdAt", n.getCreatedAt().toString());
            node.put("processed", n.isProcessed());
            arr.add(node);
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.set("notifications", arr);
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(objectMapper.writeValueAsString(response));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markRead(@RequestHeader("Authorization") String authToken,
                                           @PathVariable UUID id) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isEmpty() || !opt.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body("{\"error\":\"Notification not found\"}");
        }
        opt.get().setProcessed(true);
        notificationRepository.save(opt.get());
        return ResponseEntity.ok("{\"message\":\"Marked as read\"}");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllRead(@RequestHeader("Authorization") String authToken) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        List<Notification> unread = notificationRepository.findByUserIdAndProcessedFalse(userId);
        unread.forEach(n -> n.setProcessed(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok("{\"message\":\"All notifications marked as read\"}");
    }
}
