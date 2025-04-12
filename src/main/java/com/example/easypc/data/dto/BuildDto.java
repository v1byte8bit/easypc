package com.example.easypc.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildDto {
    private Long id;
    private String name;
    private Double totalPrice;
    private List<BuildItemDto> items;
}