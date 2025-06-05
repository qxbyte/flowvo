package io.mcp.filemcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件操作服务测试类
 */
@SpringBootTest
class FileOperationsServiceTest {

    private FileOperationsService fileOperationsService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileOperationsService = new FileOperationsService();
        // 设置临时目录作为基础路径
        ReflectionTestUtils.setField(fileOperationsService, "basePath", tempDir.toString());
    }

    @Test
    void testCreateFile() {
        String result = fileOperationsService.createFile("test.txt", "Hello World");
        
        assertTrue(result.contains("文件创建成功"));
    }

    @Test
    void testReadTextFile() {
        // 先创建文本文件
        fileOperationsService.createFile("test.txt", "Hello World");
        
        // 然后读取文件
        String content = fileOperationsService.readFile("test.txt");
        
        assertEquals("Hello World", content);
    }

    @Test
    void testReadJsonFile() {
        // 创建JSON文件
        String jsonContent = "{\n  \"name\": \"test\",\n  \"value\": 123\n}";
        fileOperationsService.createFile("test.json", jsonContent);
        
        // 读取JSON文件
        String content = fileOperationsService.readFile("test.json");
        
        assertEquals(jsonContent, content);
    }

    @Test
    void testReadMarkdownFile() {
        // 创建Markdown文件
        String markdownContent = "# 标题\n\n这是一个测试文档\n\n- 列表项1\n- 列表项2";
        fileOperationsService.createFile("test.md", markdownContent);
        
        // 读取Markdown文件
        String content = fileOperationsService.readFile("test.md");
        
        assertEquals(markdownContent, content);
    }

    @Test
    void testWriteFile() {
        String result = fileOperationsService.writeFile("test.txt", "New Content");
        
        assertTrue(result.contains("文件写入成功"));
        
        // 验证内容
        String content = fileOperationsService.readFile("test.txt");
        assertEquals("New Content", content);
    }

    @Test
    void testCreateDirectory() {
        String result = fileOperationsService.createDirectory("testdir");
        
        assertTrue(result.contains("目录创建成功"));
    }

    @Test
    void testListDirectory() {
        // 创建一些文件和目录
        fileOperationsService.createFile("file1.txt", "content1");
        fileOperationsService.createDirectory("subdir");
        
        String result = fileOperationsService.listDirectory(".");
        
        assertTrue(result.contains("[FILE] file1.txt"));
        assertTrue(result.contains("[DIR] subdir"));
    }

    @Test
    void testInsertLines() {
        // 创建多行文件
        fileOperationsService.createFile("multiline.txt", "Line 1\nLine 2\nLine 3");
        
        // 在第2行插入内容
        String result = fileOperationsService.insertLines("multiline.txt", 2, "Inserted Line");
        
        assertTrue(result.contains("插入内容成功"));
        
        // 验证内容
        String content = fileOperationsService.readFile("multiline.txt");
        
        String[] lines = content.split("\n");
        assertEquals("Line 1", lines[0]);
        assertEquals("Inserted Line", lines[1]);
        assertEquals("Line 2", lines[2]);
        assertEquals("Line 3", lines[3]);
    }

    @Test
    void testDeleteLines() {
        // 创建多行文件
        fileOperationsService.createFile("multiline.txt", "Line 1\nLine 2\nLine 3\nLine 4");
        
        // 删除第2行开始的2行
        String result = fileOperationsService.deleteLines("multiline.txt", 2, 2);
        
        assertTrue(result.contains("删除行成功"));
        
        // 验证内容
        String content = fileOperationsService.readFile("multiline.txt");
        
        String[] lines = content.split("\n");
        assertEquals(2, lines.length);
        assertEquals("Line 1", lines[0]);
        assertEquals("Line 4", lines[1]);
    }

    @Test
    void testMoveFile() {
        // 创建源文件
        fileOperationsService.createFile("source.txt", "Content to move");
        
        // 移动文件
        String result = fileOperationsService.moveFile("source.txt", "destination.txt");
        
        assertTrue(result.contains("文件移动成功"));
        
        // 验证源文件不存在
        String readSource = fileOperationsService.readFile("source.txt");
        assertTrue(readSource.contains("文件不存在"));
        
        // 验证目标文件存在且内容正确
        String readDest = fileOperationsService.readFile("destination.txt");
        assertEquals("Content to move", readDest);
    }

    @Test
    void testDeleteFile() {
        // 创建文件
        fileOperationsService.createFile("todelete.txt", "To be deleted");
        
        // 删除文件
        String result = fileOperationsService.deleteFile("todelete.txt");
        
        assertTrue(result.contains("文件删除成功"));
        
        // 验证文件不存在
        String readResult = fileOperationsService.readFile("todelete.txt");
        assertTrue(readResult.contains("文件不存在"));
    }

    @Test
    void testGetFileInfoForTextFile() {
        // 创建文件
        fileOperationsService.createFile("info.txt", "Some content\nSecond line");
        
        String result = fileOperationsService.getFileInfo("info.txt");
        
        assertTrue(result.contains("类型: 文件"));
        assertTrue(result.contains("行数: 2"));
        assertTrue(result.contains("可读: true"));
        assertTrue(result.contains("可写: true"));
    }

    @Test
    void testGetFileInfoForDirectory() {
        // 创建目录
        fileOperationsService.createDirectory("testdir");
        
        String result = fileOperationsService.getFileInfo("testdir");
        
        assertTrue(result.contains("类型: 目录"));
        assertTrue(result.contains("可读: true"));
        assertTrue(result.contains("可写: true"));
    }

    @Test
    void testReadNonExistentFile() {
        String result = fileOperationsService.readFile("nonexistent.txt");
        
        assertTrue(result.contains("文件不存在"));
    }

    @Test
    void testCreateFileWithMissingDirectory() {
        String result = fileOperationsService.createFile("subdir/nested/test.txt", "Content");
        
        assertTrue(result.contains("文件创建成功"));
        
        // 验证文件内容
        String content = fileOperationsService.readFile("subdir/nested/test.txt");
        assertEquals("Content", content);
    }

    @Test
    void testFileInfoWithUnsupportedFormat() throws Exception {
        // 创建一个二进制文件模拟不支持的格式
        Path binaryFile = tempDir.resolve("test.bin");
        Files.write(binaryFile, new byte[]{0x00, 0x01, 0x02, 0x03});
        
        String result = fileOperationsService.getFileInfo("test.bin");
        
        assertTrue(result.contains("类型: 文件"));
        assertTrue(result.contains("格式: BIN"));
        assertTrue(result.contains("解析支持: 否"));
    }
} 