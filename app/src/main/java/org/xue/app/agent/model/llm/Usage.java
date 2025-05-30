package org.xue.app.agent.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型使用统计类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usage {
    /**
     * 提示词token数
     */
    private Integer prompt_tokens;
    
    /**
     * 补全token数
     */
    private Integer completion_tokens;
    
    /**
     * 总token数
     */
    private Integer total_tokens;
    
    /**
     * 提示词token详情
     */
    private TokenDetails prompt_tokens_details;
    
    /**
     * 补全token详情
     */
    private TokenDetails completion_tokens_details;
    
    /**
     * token详情内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TokenDetails {
        private Integer cached_tokens;
        private Integer audio_tokens;
        private Integer reasoning_tokens;
        private Integer accepted_prediction_tokens;
        private Integer rejected_prediction_tokens;
    }
} 