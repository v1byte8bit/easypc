package com.example.easypc.data.controller.assembler_profile;

import com.example.easypc.data.dto.SourceDto;
import com.example.easypc.data.request.NotificationRequest;
import com.example.easypc.data.service.AssemblerNotificationService;
import com.example.easypc.data.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notifications")
public class AssemblerNotificationController {

    @Autowired
    private AssemblerNotificationService assemblerNotificationService;

    @Autowired
    private SourceService sourceService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> createNotification(@PathVariable Integer orderId,
                                                @RequestBody NotificationRequest dto,
                                                Authentication authentication) {
        assemblerNotificationService.createNotification(dto.getMessage(), orderId, authentication, dto.getReplacementProductUrlIds());
        return ResponseEntity.ok("Уведомление отправлено");
    }

    @GetMapping("/source/all")
    public ResponseEntity<List<SourceDto>> getAllSources() {
        List<SourceDto> sources = sourceService.getAllSources();
        return ResponseEntity.ok(sources);
    }
}