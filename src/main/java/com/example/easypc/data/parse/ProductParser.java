package com.example.easypc.data.parse;

import com.example.easypc.data.entity.Source;

public interface ProductParser {
    ProductData parse(Source source, String category);
    boolean supports(String url);
}