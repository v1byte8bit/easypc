package com.example.easypc.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CartProductData {
    private String name;
    private String price;
    private String category;
    private String img;
    private Long urlId;
    private Map<String, String> characteristics;
    private int quantity;

    public CartProductData(String name, String price, String category, String img, Long urlId, Map<String, String> characteristics, int quantity) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.img = img;
        this.urlId = urlId;
        this.characteristics = characteristics;
        this.quantity = quantity;
    }
}