package com.example.easypc.data.controller.assembler_profile;

import com.example.easypc.data.dto.OrderDto;
import com.example.easypc.data.service.AssemblerOrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class OrderAssemblerController {
    private final AssemblerOrderService orderService;

    @GetMapping("/created/orders")
    public ResponseEntity<List<OrderDto>> getCreatedOrders() {
        List<OrderDto> orders = orderService.getCreatedOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/assembler/orders")
    public String showAssemblerOrdersPage() {
        return "assembler_orders";
    }

    @PostMapping("/{orderId}/take")
    public ResponseEntity<String> takeOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        orderService.takeOrder(orderId, authentication);
        return ResponseEntity.ok("Заказ взят в работу");
    }
}
