package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话列表DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListDTO {
    
    /**
     * 对话列表
     */
    private List<ConversationDTO> items;
    
    /**
     * 总数
     */
    private Long total;
    
} 