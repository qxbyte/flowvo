package org.xue.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.app.service.DocumentParserService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SpringBootTest
public class DocumentParserTest {

    @Autowired
    private DocumentParserService documentParserService;

    @Test
    public void testTextParser() {
        try {
            String testContent = "Hello World\nThis is a test document.";
            InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
            
            String result = documentParserService.parseDocument(inputStream, "test.txt", "text/plain");
            System.out.println("解析结果: " + result);
            
            assert result != null;
            assert result.contains("Hello World");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void testSupportedTypes() {
        System.out.println("支持的解析器类型: " + documentParserService.getSupportedTypes());
        
        // 测试支持的文件类型
        assert documentParserService.isSupported("test.txt", "text/plain");
        assert documentParserService.isSupported("test.pdf", "application/pdf");
        assert documentParserService.isSupported("test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
} 