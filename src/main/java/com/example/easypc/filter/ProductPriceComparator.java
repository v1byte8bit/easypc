package com.example.easypc.filter;

import com.example.easypc.parse.ProductData;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@AllArgsConstructor
public class ProductPriceComparator {

    private final ProductComparator productComparator;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, List<ProductData>> productCache = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void processProduct(ProductData incomingProduct) {
        String category = incomingProduct.getCategory();
        productCache.putIfAbsent(category, new ArrayList<>());
        List<ProductData> existingProducts = productCache.get(category);

        boolean merged = false;
        List<ProductData> updatedProducts = new ArrayList<>();

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
        debounceSend(category);
    }

    private void debounceSend(String category) {
        ScheduledFuture<?> scheduled = scheduledTasks.get(category);
        if (scheduled != null && !scheduled.isDone()) {
            scheduled.cancel(false);
        }
        ScheduledFuture<?> newScheduled = scheduler.schedule(() -> {
            List<ProductData> products = productCache.getOrDefault(category, List.of());
            List<ProductData> sortedProducts = products.stream()
                    .sorted(Comparator.comparingDouble(p -> parsePrice(p.getPrice())))
                    .toList();
            messagingTemplate.convertAndSend("/topic/products/" + category, sortedProducts);
        }, 300, TimeUnit.MILLISECONDS);

        scheduledTasks.put(category, newScheduled);
    }

    private double parsePrice(String price) {
        try {
            return Double.parseDouble(price.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    public List<ProductData> getProductsByCategory(String category) {
        return productCache.getOrDefault(category, List.of());
    }

    public void processProductList(List<ProductData> products) {
        if (products == null || products.isEmpty()) return;

        String category = products.get(0).getCategory(); // Предполагаем, что все из одной категории
        productCache.putIfAbsent(category, new ArrayList<>());
        List<ProductData> existingProducts = productCache.get(category);
        List<ProductData> updatedProducts = new ArrayList<>(existingProducts);

        for (ProductData incomingProduct : products) {
            boolean merged = false;
            for (int i = 0; i < updatedProducts.size(); i++) {
                ProductData existing = updatedProducts.get(i);
                if (productComparator.areSameProduct(existing, incomingProduct)) {
                    double incomingPrice = parsePrice(incomingProduct.getPrice());
                    double existingPrice = parsePrice(existing.getPrice());
                    if (incomingPrice < existingPrice) {
                        updatedProducts.set(i, incomingProduct);
                    }
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                updatedProducts.add(incomingProduct);
            }
        }

        productCache.put(category, updatedProducts);
        debounceSend(category);
    }
}