package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.AlertStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertStatusUpdateRequest(
        @NotNull(message = "预警状态不能为空")
        AlertStatus status,
        @Size(max = 500, message = "处理说明长度不能超过500个字符")
        String handledNote) {
}
