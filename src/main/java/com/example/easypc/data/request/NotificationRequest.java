package com.example.easypc.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private String message;
    private Integer userId;
    private Integer assemblerId;
}