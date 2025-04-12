package com.example.easypc.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    private String phone;
    private String password;
    private String confirmPassword;
    private String role;

}

