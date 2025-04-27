package com.example.easypc.parse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Data
public class ProductData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String price;
    private String category;
    private String img;
    private Long urlId;
    private Map<String, String> characteristics;

    @JsonCreator
    public ProductData(@JsonProperty("name") String name,
                       @JsonProperty("price") String price,
                       @JsonProperty("category") String category,
                       @JsonProperty("img") String img,
                       @JsonProperty("urlId") Long urlId,
                       @JsonProperty("characteristics") Map<String, String> characteristics)
    {
        this.name = name;
        this.price = price;
        this.category = category;
        this.img = img;
        this.urlId = urlId;
        this.characteristics = characteristics;
    }
}