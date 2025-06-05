package org.xue.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Core模块提示词服务类
 * 从配置文件读取各种提示词，便于统一管理和修改
 */
@Service
public class PromptsService {

    // 函数调用相关提示词
    @Value("${prompts.function-call.system:你是一个助手，如果需要调用函数，请使用 function_call 返回 JSON。}")
    private String functionCallSystemPrompt;

    @Value("${prompts.function-call.decision:你是一个助手。判断用户的问题是否需要调用函数。如果需要，请只回复\"是\"；如果不需要，请只回复\"否\"。不要提供任何解释或额外信息。}")
    private String functionCallDecisionPrompt;

    @Value("${prompts.function-call.selection:你是一个助手，请根据用户问题选择并返回需要调用的函数。请直接返回function_call字段的JSON格式，**不要**返回多余解释。}")
    private String functionCallSelectionPrompt;

    @Value("${prompts.function-call.decision-simple:你是一个助手。判断用户的问题是否需要调用函数。如果需要，请只回复\"Y\"；如果不需要，请只回复\"N\"。不要提供任何解释或额外信息。}")
    private String functionCallDecisionSimplePrompt;

    // 聊天相关提示词
    @Value("${prompts.chat.default:你是一个友善、专业的AI助手，请根据用户的问题提供准确、有用的回答。}")
    private String chatDefaultPrompt;

    @Value("${prompts.chat.knowledge-base:【以下是相关资料，可参考作答】\n{context}\n【用户提问】\n{question}}")
    private String chatKnowledgeBasePrompt;

    @Value("${prompts.chat.error-fallback:抱歉，我在处理您的请求时遇到了一些问题。请稍后再试，或者重新描述您的问题。}")
    private String chatErrorFallbackPrompt;

    // 函数调用相关方法
    public String getFunctionCallSystemPrompt() {
        return functionCallSystemPrompt;
    }

    public String getFunctionCallDecisionPrompt() {
        return functionCallDecisionPrompt;
    }

    public String getFunctionCallSelectionPrompt() {
        return functionCallSelectionPrompt;
    }

    public String getFunctionCallDecisionSimplePrompt() {
        return functionCallDecisionSimplePrompt;
    }

    // 聊天相关方法
    public String getChatDefaultPrompt() {
        return chatDefaultPrompt;
    }

    public String getChatErrorFallbackPrompt() {
        return chatErrorFallbackPrompt;
    }

    // 工具方法：格式化提示词
    public String formatPrompt(String template, String... params) {
        String result = template;
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                result = result.replace("{" + params[i] + "}", params[i + 1]);
            }
        }
        return result;
    }

    // 快捷方法：获取知识库提示词
    public String getChatKnowledgeBasePrompt(String context, String question) {
        return formatPrompt(chatKnowledgeBasePrompt, "context", context, "question", question);
    }
} 