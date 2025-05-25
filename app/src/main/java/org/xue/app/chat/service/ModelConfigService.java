package org.xue.app.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型配置服务
 * 负责读取和管理AI模型配置
 */
@Service
@Slf4j
public class ModelConfigService {
    
    @Value("${ai.openai.chat.options.model:gpt-4-turbo}")
    private String openaiModels;
    
    @Value("${ai.deepseek.chat.options.model:deepseek-chat}")
    private String deepseekModels;
    
    /**
     * 获取所有可用的模型配置
     */
    public Map<String, Object> getAllModels() {
        List<Map<String, Object>> openaiModelList = parseModels(openaiModels, "openai");
        List<Map<String, Object>> deepseekModelList = parseModels(deepseekModels, "deepseek");
        
        return Map.of(
            "openai", openaiModelList,
            "deepseek", deepseekModelList,
            "total", openaiModelList.size() + deepseekModelList.size()
        );
    }
    
    /**
     * 获取扁平化的模型列表（用于前端选择器）
     */
    public List<Map<String, Object>> getFlatModels() {
        List<Map<String, Object>> allModels = Arrays.asList(openaiModels.split(","))
            .stream()
            .map(String::trim)
            .filter(model -> !model.isEmpty())
            .map(model -> createModelInfo(model, "openai", getModelDescription(model)))
            .collect(Collectors.toList());
        
        // 添加DeepSeek模型
        allModels.addAll(
            Arrays.asList(deepseekModels.split(","))
                .stream()
                .map(String::trim)
                .filter(model -> !model.isEmpty())
                .map(model -> createModelInfo(model, "deepseek", getModelDescription(model)))
                .collect(Collectors.toList())
        );
        
        return allModels;
    }
    
    /**
     * 获取支持Vision的模型列表
     */
    public List<Map<String, Object>> getVisionSupportedModels() {
        List<String> visionSupportedModels = Arrays.asList(
            "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4-vision-preview"
        );
        
        return Arrays.asList(openaiModels.split(","))
            .stream()
            .map(String::trim)
            .filter(model -> !model.isEmpty())
            .filter(visionSupportedModels::contains)
            .map(model -> createModelInfo(model, "openai", getModelDescription(model) + " (Vision支持)"))
            .collect(Collectors.toList());
    }
    
    /**
     * 检查模型是否支持Vision功能
     */
    public boolean isVisionSupported(String model) {
        List<String> visionSupportedModels = Arrays.asList(
            "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4-vision-preview"
        );
        return visionSupportedModels.contains(model);
    }
    
    /**
     * 根据模型获取对应的提供商配置
     */
    public String getProviderForModel(String model) {
        if (Arrays.asList(openaiModels.split(",")).contains(model.trim())) {
            return "openai";
        } else if (Arrays.asList(deepseekModels.split(",")).contains(model.trim())) {
            return "deepseek";
        }
        return "openai"; // 默认使用OpenAI
    }
    
    private List<Map<String, Object>> parseModels(String modelsString, String provider) {
        return Arrays.asList(modelsString.split(","))
            .stream()
            .map(String::trim)
            .filter(model -> !model.isEmpty())
            .map(model -> createModelInfo(model, provider, getModelDescription(model)))
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> createModelInfo(String model, String provider, String description) {
        return Map.of(
            "id", model,
            "name", formatModelName(model),
            "description", description,
            "provider", provider,
            "visionSupported", isVisionSupported(model)
        );
    }
    
    private String formatModelName(String model) {
        switch (model) {
            case "gpt-4o": return "GPT-4o";
            case "gpt-4o-mini": return "GPT-4o Mini";
            case "gpt-4-turbo": return "GPT-4 Turbo";
            case "gpt-3.5-turbo": return "GPT-3.5 Turbo";
            case "deepseek-chat": return "DeepSeek Chat";
            default: return model.toUpperCase();
        }
    }
    
    private String getModelDescription(String model) {
        switch (model) {
            case "gpt-4o": return "Most capable multimodal model";
            case "gpt-4o-mini": return "Fast and efficient vision model";
            case "gpt-4-turbo": return "Most capable text model";
            case "gpt-3.5-turbo": return "Balanced performance";
            case "deepseek-chat": return "DeepSeek AI model";
            default: return "AI model";
        }
    }
} 