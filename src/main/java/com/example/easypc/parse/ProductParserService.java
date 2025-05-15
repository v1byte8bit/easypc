package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.filter.ProductPriceComparator;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
public class ProductParserService {

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private final List<ProductParser> parsers;

    @Autowired
    private ProductPriceComparator productPriceComparator;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //Каждый запрос на парсинг запускается в отдельном потоке с помощью ExecutorService и CompletableFuture
    @Async
    public CompletableFuture<List<ProductData>> parseAndSendData(String category) {
        List<Source> sources = sourceRepository.findByCategory(category);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<CompletableFuture<ProductData>> futures = sources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> parseProduct(source, category), executor))
                .toList();

        List<ProductData> productList = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        executor.shutdown();
        productList.forEach(product ->
                rabbitTemplate.convertAndSend("productExchange", "product.data", product)
        );
        return CompletableFuture.completedFuture(productList);
    }


    public ProductData parseSingleProduct(Long urlId) {
        Source source = sourceRepository.findById(urlId).orElse(null);
        return source != null ? parseProduct(source, source.getCategory()) : null;
    }

    public ProductData parseProduct(Source source, String category) {
        return parsers.stream()
                .filter(p -> p.supports(source.getSource()))
                .findFirst()
                .map(p -> p.parse(source, category))
                .orElseThrow(() -> new RuntimeException("No parser found for URL: " + source.getSource()));
    }
}