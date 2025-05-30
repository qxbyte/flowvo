package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话更新数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationUpdateDTO {
    /**
     * 对话名称
     */
    private String title;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 服务名称
     */
    private String service;
} 