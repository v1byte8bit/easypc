package com.example.easypc.data.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MakingOrderController {
    @GetMapping("/making/order")
    public String showOrdersPage() {
        return "making_order";
    }
}
