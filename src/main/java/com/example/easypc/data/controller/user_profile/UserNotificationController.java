package com.example.easypc.data.controller.user_profile;

import com.example.easypc.data.dto.NotificationDto;
import com.example.easypc.data.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.List;


@Controller
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService notificationService;

    @GetMapping("/user/notification")
    public String showCartPage() {
        return "user_notifications";
    }

    @GetMapping("/user/notification/get")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        List<NotificationDto> notifications = notificationService.getNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }
}
