package org.xue.functioncall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xue.functioncall.service.FunctionCallService;

@RestController
@RequestMapping("/api/function-call")
@RequiredArgsConstructor
public class FunctionCallController {

    private final FunctionCallService functionCallService;

    @PostMapping("/invoke")
    public void invokeFunction(@RequestParam String question) {
        functionCallService.handleUserQuestion(question);
    }
}

