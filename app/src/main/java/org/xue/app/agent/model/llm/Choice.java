package org.xue.app.agent.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型选择结果类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Choice {
    /**
     * 索引
     */
    private Integer index;
    
    /**
     * 消息
     */
    private AssistantMessage message;
    
    /**
     * logprobs
     */
    private Object logprobs;
    
    /**
     * 完成原因
     */
    private String finish_reason;
} 