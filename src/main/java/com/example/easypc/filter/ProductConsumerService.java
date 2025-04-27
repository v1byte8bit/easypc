package com.example.easypc.filter;

import com.example.easypc.parse.ProductData;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductConsumerService {

    private final ProductComparator productComparator;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, List<ProductData>> productCache = new ConcurrentHashMap<>();

    @RabbitListener(queues = "test-queue")
    public void consumeProduct(ProductData incomingProduct) {
        String category = incomingProduct.getCategory();
        productCache.putIfAbsent(category, new ArrayList<>());
        List<ProductData> existingProducts = productCache.get(category);

        List<ProductData> updatedProducts = new ArrayList<>();
        boolean merged = false;

        //Поиск одинаковых товаров
        for (ProductData existing : existingProducts) {
            if (productComparator.areSameProduct(existing, incomingProduct)) {
                double incomingPrice = parsePrice(incomingProduct.getPrice());
                double existingPrice = parsePrice(existing.getPrice());
                if (incomingPrice < existingPrice) {
                    updatedProducts.add(incomingProduct);
                } else {
                    updatedProducts.add(existing);
                }
                merged = true;
            } else {
                updatedProducts.add(existing);
            }
        }
        if (!merged) {
            updatedProducts.add(incomingProduct);
        }
        productCache.put(category, updatedProducts);
        //Сортировка по цене
        List<ProductData> sortedProducts = updatedProducts.stream()
                .sorted(Comparator.comparingDouble(p -> parsePrice(p.getPrice())))
                .toList();
        messagingTemplate.convertAndSend("/topic/products/" + category, sortedProducts);
    }

    private double parsePrice(String price) {
        try {
            return Double.parseDouble(price.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }
}