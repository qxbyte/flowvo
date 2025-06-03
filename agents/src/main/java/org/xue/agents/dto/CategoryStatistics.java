package org.xue.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatistics {
    
    /**
     * 分类ID
     */
    private String categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类图标
     */
    private String categoryIcon;
    
    /**
     * 文档数量
     */
    private Integer documentCount;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdatedTime;
    
    /**
     * 完成率
     */
    private Double completionRate;
    
    /**
     * 该分类的文档列表（点击查看时返回）
     */
    private List<DocumentInfo> documents;
    
    /**
     * 文档基本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentInfo {
        /**
         * 文档ID
         */
        private String id;
        
        /**
         * 文档名称
         */
        private String name;
        
        /**
         * 文档大小
         */
        private Long size;
        
        /**
         * 文档类型
         */
        private String type;
        
        /**
         * 处理状态
         */
        private String status;
        
        /**
         * 创建时间
         */
        private LocalDateTime createdAt;
        
        /**
         * 更新时间
         */
        private LocalDateTime updatedAt;
    }
} 