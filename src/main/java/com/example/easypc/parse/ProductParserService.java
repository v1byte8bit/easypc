package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.filter.ProductComparator;
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
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductParserService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private final List<ProductParser> parsers;

    @Autowired
    private ProductComparator productComparator;

    /*
        Каждый запрос на парсинг запускается в отдельном потоке с помощью ExecutorService и CompletableFuture
    */
    @Async
    public CompletableFuture<List<ProductData>> parseAndSendData(String category) {
        List<Source> sources = sourceRepository.findByCategory(category);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<CompletableFuture<Void>> futures = sources.stream()
                .map(source -> CompletableFuture.runAsync(() -> parseAndSend(source, category), executor))
                .toList();

        futures.forEach(CompletableFuture::join);
        executor.shutdown();

        List<ProductData> productList = sources.stream()
                .map(source -> parseProduct(source, category))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        productList.forEach(productData ->
                rabbitTemplate.convertAndSend("productExchange", "product.data", productData)
        );

        return CompletableFuture.completedFuture(productList);
    }

    private void parseAndSend(Source source, String category) {
        ProductData productData = parseProduct(source, category);
        if (productData != null) {
            CompletableFuture.runAsync(() ->
                    rabbitTemplate.convertAndSend("productExchange", "product.data", productData)
            );
        }
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