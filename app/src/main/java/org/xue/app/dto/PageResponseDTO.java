package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 数据列表
     */
    private List<T> items;
    
    /**
     * 是否为第一页
     */
    private boolean isFirst;
    
    /**
     * 是否为最后一页
     */
    private boolean isLast;
} 