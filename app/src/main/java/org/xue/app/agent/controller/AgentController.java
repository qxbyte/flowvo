package org.xue.app.agent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xue.app.agent.model.AgentRequest;
import org.xue.app.agent.model.AgentResponse;
import org.xue.app.agent.service.AgentService;

/**
 * Agent控制器
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    
    @Autowired
    private AgentService agentService;
    
    /**
     * 处理用户请求
     *
     * @param request 用户请求
     * @return 处理结果
     */
    @PostMapping("/process")
    public ResponseEntity<AgentResponse> process(@RequestBody AgentRequest request) {
        log.info("接收到用户请求: {}", request);
        
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AgentResponse.error("用户问题不能为空"));
        }
        
        if (request.getService() == null || request.getService().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AgentResponse.error("服务名称不能为空"));
        }
        
        AgentResponse response = agentService.process(request);
        return ResponseEntity.ok(response);
    }
} 