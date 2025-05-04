package com.example.easypc.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NotificationDto {
    private Integer id;
    private String message;
    private Long orderId;
    private List<Integer> replacementProductUrlIds;
}


