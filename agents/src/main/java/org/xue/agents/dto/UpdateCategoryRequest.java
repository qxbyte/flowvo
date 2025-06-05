package org.xue.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xue.agents.entity.DocumentCategory;

/**
 * 更新分类请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 状态
     */
    private DocumentCategory.Status status;
} 