package com.example.easypc.rabbitmq;

import com.example.easypc.data.parse.ProductData;
import com.example.easypc.websocket.WebSocketSender;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductListener {

    @Autowired
    private WebSocketSender webSocketSender;

    @RabbitListener(queues = "test-queue")
    public void receiveProductData(ProductData productData) {
        webSocketSender.sendProductData(productData);
    }
}
