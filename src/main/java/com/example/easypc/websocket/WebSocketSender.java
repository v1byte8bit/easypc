package com.example.easypc.websocket;

import com.example.easypc.parse.ProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSender {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendProductData(ProductData productData) {
        messagingTemplate.convertAndSend("/topic/products", productData);
    }
}