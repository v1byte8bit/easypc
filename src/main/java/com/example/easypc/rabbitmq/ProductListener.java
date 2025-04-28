package com.example.easypc.rabbitmq;


import com.example.easypc.filter.ProductPriceComparator;
import com.example.easypc.parse.ProductData;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductListener {

    private final ProductPriceComparator productService;

    @RabbitListener(queues = "test-queue")
    public void consumeProduct(ProductData incomingProduct) {
        productService.processProduct(incomingProduct);
    }
}