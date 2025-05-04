package com.example.easypc.data.controller.user_profile;

import com.example.easypc.data.dto.NotificationDto;
import com.example.easypc.data.service.UserNotificationService;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;


@Controller
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService notificationService;

    @Autowired
    private ProductParserService productParserService;

    @GetMapping("/user/notification")
    public String showCartPage() {
        return "user_notifications";
    }

    @GetMapping("/user/notification/get")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        List<NotificationDto> notifications = notificationService.getNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/scrape")
    public ResponseEntity<ProductData> scrapeByProductUrl(@RequestParam long urlId) {
        try {
            ProductData data = productParserService.parseSingleProduct(urlId);
            if (data == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}