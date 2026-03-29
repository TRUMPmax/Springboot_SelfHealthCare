package com.example.selfhealthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(
                regexp = "^[a-zA-Z0-9_\\-]{4,40}$",
                message = "用户名仅支持4到40位字母、数字、下划线或中划线")
        String username,
        @NotBlank(message = "显示名称不能为空")
        @Size(max = 60, message = "显示名称长度不能超过60个字符")
        String displayName,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6到64位之间")
        String password,
        @NotBlank(message = "确认密码不能为空")
        @Size(min = 6, max = 64, message = "确认密码长度需在6到64位之间")
        String confirmPassword) {
}
