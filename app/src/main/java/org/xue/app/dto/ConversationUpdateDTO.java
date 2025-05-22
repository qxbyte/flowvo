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
} 