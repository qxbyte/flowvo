package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱检查请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckEmailRequest {
    private String email;
} 