package com.example.easypc.data.controller;

import com.example.easypc.filter.ProductPriceComparator;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
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
import java.util.List;
import java.util.Map;


@Controller
public class MainController {

    @Autowired
    private ProductParserService productParserService;

    @Autowired
    private ProductPriceComparator productPriceComparator;

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
    public ResponseEntity<Void> startParsing(@RequestParam String category) {
        try {
            productParserService.parseAndSendData(category);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductData>> getProducts(@RequestParam String category) {
        List<ProductData> products = productPriceComparator.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
}