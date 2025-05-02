package com.example.easypc.data.controller;

import com.example.easypc.data.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/notifications")
public class AssemblerNotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<String> createNotification(@RequestBody Map<String, String> request,
                                                     @PathVariable Integer orderId,
                                                     Authentication authentication) {
        String message = request.get("message");
        notificationService.createNotification(message, orderId, authentication);
        return ResponseEntity.ok("Уведомление отправлено");
    }
}