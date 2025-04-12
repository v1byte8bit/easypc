package com.example.easypc.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private Long userId;
    private Long orderId;
    private String message;
    private Long variant1Id;
    private Long variant2Id;
}