package com.example.easypc.data.controller;

import com.example.easypc.data.dto.CartProductData;
import com.example.easypc.data.service.CartService;
import com.example.easypc.data.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping("/cart")
    public String showCartPage() {
        return "cart";
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Integer sourceId) {
        cartService.addToCart(sourceId);
        return ResponseEntity.ok("Item added to cart");
    }

    @GetMapping("/cart/total")
    public ResponseEntity<BigDecimal> getCartTotal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();

        Long userId = userService.getUserIdByUsername(phone);
        BigDecimal total = cartService.getCartTotal(userId);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/cart/items")
    public ResponseEntity<List<CartProductData>> getCartItems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.getUserIdByUsername(auth.getName());
        List<CartProductData> products = cartService.getCartItems(userId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/cart/updateQuantity")
    public ResponseEntity<?> updateCartQuantity(@RequestParam Long sourceId, @RequestParam int quantity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.getUserIdByUsername(auth.getName());
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        return cartService.updateCartQuantity(userId, sourceId, quantity);
    }

    @DeleteMapping("/cart/remove")
    public ResponseEntity<String> removeFromCart(@RequestParam Long sourceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.getUserIdByUsername(auth.getName());

        cartService.removeFromCart(sourceId, userId);
        return ResponseEntity.ok("Товар удален из корзины");
    }


}