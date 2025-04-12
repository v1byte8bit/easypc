package com.example.easypc.data.controller;

import com.example.easypc.data.request.ProfileUpdateRequest;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.service.UserDetailsServiceImpl;
import com.example.easypc.data.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public abstract class ProfileController {
    protected final UserDetailsServiceImpl userServiceImpl;
    protected final UserService userService;

    @PostMapping("/api/update")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        try {
            String phone = SecurityContextHolder.getContext().getAuthentication().getName();
            userServiceImpl.updateUserProfile(phone, request);
            return ResponseEntity.ok("Данные успешно обновлены.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/current")
    public ResponseEntity<?> getCurrentProfile() {
        try {
            String phone = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> user = userService.findByPhone(phone);

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Пользователь не найден"));
            }

            String email = user.get().getEmail();
            String safeEmail = (email != null) ? email : "";

            Map<String, String> response = new HashMap<>();
            response.put("email", safeEmail);
            response.put("phone", user.get().getPhone());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
