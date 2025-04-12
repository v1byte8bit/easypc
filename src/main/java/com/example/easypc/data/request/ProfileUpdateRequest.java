package com.example.easypc.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    private String email;
    private String phone;
    private String password;
}
