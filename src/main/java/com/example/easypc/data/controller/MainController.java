package com.example.easypc.data.controller;

import com.example.easypc.data.parse.ProductParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;


@Controller
public class MainController {

    @Autowired
    private ProductParserService productParserService;

    @GetMapping("/main")
    public String mainPage(Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("authenticated", true);
        } else {
            model.addAttribute("authenticated", false);
        }
        return "main";
    }

    @PostMapping("/parse")
    public ResponseEntity<Map<String, String>> startParsing(@RequestParam String category) {
        try {
            //Парсинг для переданной категории
            productParserService.parseAndSendData(category);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Запущен парсинг для категории: " + category);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ошибка при запуске парсинга: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}