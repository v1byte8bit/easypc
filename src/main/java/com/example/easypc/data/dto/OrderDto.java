package com.example.easypc.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private String status;
    private Double totalPrice;
    private String address;
    private String phone;
    private List<OrderItemDto> items;
}
