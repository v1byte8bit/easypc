package com.example.easypc.data.controller.assembler_profile;

import com.example.easypc.data.dto.OrderDto;
import com.example.easypc.data.request.UpdateStatusRequest;
import com.example.easypc.data.service.AssemblerWorkService;
import com.example.easypc.data.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
@AllArgsConstructor
public class WorkAssemblerController {
    private final AssemblerWorkService assemblerWorkService;
    private final UserService userService;

    @GetMapping("/on_work")
    public String showAssemblerWorkPage() {
        return "assembler_work";
    }

    @GetMapping("/assembler/on/work")
    public ResponseEntity<List<OrderDto>> getOrdersByAssembler(Authentication authentication) {
        String phone = authentication.getName();
        Long assemblerId = userService.getUserIdByUsername(phone);
        List<OrderDto> orders = assemblerWorkService.getAssemblerWork(assemblerId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/assembler/work/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateStatusRequest request) {
        assemblerWorkService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
