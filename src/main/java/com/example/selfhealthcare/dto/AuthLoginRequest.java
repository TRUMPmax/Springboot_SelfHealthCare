package com.example.selfhealthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 40, message = "用户名长度不能超过40个字符")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(max = 64, message = "密码长度不能超过64个字符")
        String password) {
}
