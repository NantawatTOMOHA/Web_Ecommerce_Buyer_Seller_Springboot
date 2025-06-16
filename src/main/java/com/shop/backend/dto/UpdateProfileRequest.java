package com.shop.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String email;
    private String fullName;
    private String password;
}
