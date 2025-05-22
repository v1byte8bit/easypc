package com.example.easypc.data.configuration;

import com.example.easypc.data.entity.Source;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheUpdateService {

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private ProductParserService productParserService;

    @CachePut(value = "productCache", key = "#urlId")
    public ProductData updateProductCache(Long urlId) {
        Source source = sourceRepository.findById(urlId).orElse(null);
        return source != null ? productParserService.parseProduct(source, source.getCategory()) : null;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refreshProductCache() {
        List<Integer> allIds = sourceRepository.findAll()
                .stream()
                .map(Source::getId)
                .toList();
        allIds.forEach(id -> updateProductCache(id.longValue()));
    }
}
