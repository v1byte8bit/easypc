package com.example.easypc.parse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

@Configuration
public class AsyncConfig {
    @Bean
    public ExecutorService parserExecutor() {
        int threads = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(threads);
    }
}
