package com.example.easypc.data.controller.user_profile;

import com.example.easypc.data.dto.OrderDto;
import com.example.easypc.data.request.OrderRequest;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.service.OrderService;
import com.example.easypc.data.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/orders")
    public String showOrdersPage() {
        return "user_orders";
    }

    @PostMapping("/order/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest, Authentication authentication) {
        Order order = orderService.createOrder(orderRequest, authentication);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/orders")
    public ResponseEntity<List<OrderDto>> getUserOrders(Authentication authentication) {
        String phone = authentication.getName();
        Long userId = userService.getUserIdByUsername(phone);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        boolean success = orderService.cancelOrder(orderId);
        if (!success) {
            return ResponseEntity.status(400).body(Map.of("message", "Ошибка при отмене заказа"));
        }
        return ResponseEntity.ok(Map.of("message", "Заказ отменен"));
    }
}