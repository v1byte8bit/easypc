package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import com.example.easypc.data.repository.SourceRepository;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@AllArgsConstructor
public class ProductParserService {

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private final List<ProductParser> parsers;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ExecutorService parserExecutor;

    //Каждый запрос на парсинг запускается в отдельном потоке с помощью ExecutorService и CompletableFuture
    @Async
    public CompletableFuture<List<ProductData>> parseAndSendData(String category) {
        List<Source> sources = sourceRepository.findByCategory(category);

        List<CompletableFuture<ProductData>> futures = sources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> parseProduct(source, category), parserExecutor))
                .toList();

        List<ProductData> productList = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        productList.forEach(product ->
                rabbitTemplate.convertAndSend("productExchange", "product.data", product)
        );
        return CompletableFuture.completedFuture(productList);
    }

    @Cacheable(value = "productCache", key = "#urlId")
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