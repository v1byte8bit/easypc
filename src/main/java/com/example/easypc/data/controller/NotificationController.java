package com.example.easypc.data.controller;

import com.example.easypc.data.entity.Notification;
import com.example.easypc.data.request.NotificationRequest;
import com.example.easypc.data.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        Notification notification = notificationService.createNotification(
                request.getUserId(),
                request.getOrderId(),
                request.getMessage(),
                request.getVariant1Id(),
                request.getVariant2Id()
        );
        return ResponseEntity.ok(Map.of("message", "Уведомление отправлено", "id", notification.getIdNote()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }
}
