package com.example.easypc.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildItemDto {
    private Integer id;
    private String name;
    private String price;
    private String imageUrl;
    private Integer quantity;
}
