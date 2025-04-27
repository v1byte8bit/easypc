package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;

public interface ProductParser {
    ProductData parse(Source source, String category);
    boolean supports(String url);
}