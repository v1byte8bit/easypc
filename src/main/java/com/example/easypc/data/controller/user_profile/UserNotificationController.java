package com.example.easypc.data.controller.user_profile;

import com.example.easypc.data.dto.NotificationDto;
import com.example.easypc.data.request.ProductReplacementRequest;
import com.example.easypc.data.service.OrderService;
import com.example.easypc.data.service.UserNotificationService;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @Autowired
    private ProductParserService productParserService;

    @GetMapping("/user/notification")
    public String showCartPage() {
        return "user_notifications";
    }

    @GetMapping("/user/notification/get")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        List<NotificationDto> notifications = userNotificationService.getNotificationsForCurrentUser();
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

    @PostMapping("/user/order/replace-product/{orderId}")
    public ResponseEntity<Void> replaceProductInOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Integer> requestBody) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String phone = authentication.getName();
            Integer replacementUrlId = requestBody.get("replacementUrlId");
            // Заменить товар в заказе
            userNotificationService.replaceProductInOrder(orderId, replacementUrlId, phone);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}