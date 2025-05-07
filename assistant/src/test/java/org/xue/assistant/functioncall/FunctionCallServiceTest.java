package org.xue.assistant.functioncall;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.assistant.functioncall.service.FunctionCallService;

@SpringBootTest
@Slf4j
public class FunctionCallServiceTest {

    @Autowired
    private FunctionCallService functionCallService;

    @Test
    void test_real_ai_reply_text_only() {
        functionCallService.handleUserQuestion("你能做什么？");
    }

    @Test
    void test_real_ai_reply_with_function_call() {
        functionCallService.handleUserQuestion("今天北京的天气怎么样");
    }
}
