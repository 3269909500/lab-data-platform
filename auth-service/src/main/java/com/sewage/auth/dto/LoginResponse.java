package com.sewage.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Builder
public class LoginResponse {

    private String token;

    private String tokenType;

    private Long expiresIn;

    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String department;
    private String email;
    private String phone;
}