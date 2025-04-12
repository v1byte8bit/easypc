package com.example.easypc.data.dto;

import com.example.easypc.data.entity.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private Integer id;
    private String name;
    private String price;
    private String imageUrl;
    private Integer quantity;
    private String url;
}
