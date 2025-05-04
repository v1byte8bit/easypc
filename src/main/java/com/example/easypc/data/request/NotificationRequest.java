package com.example.easypc.data.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotificationRequest {
    private String message;
    private Long userId;
    private Long assemblerId;
    private List<Integer> replacementProductUrlIds;
}