package com.example.easypc.data.controller.auth;

import com.example.easypc.data.request.RegistrationRequest;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "registration";
    }


    @PostMapping(value = "/api/register", consumes = "application/json")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        String role = request.getRole();

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пароли не совпадают"));
        }

        if (userService.findByPhone(phone).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пользователь уже существует"));
        }

        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);
        userService.saveUser(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Успешная регистрация"));
    }

}


