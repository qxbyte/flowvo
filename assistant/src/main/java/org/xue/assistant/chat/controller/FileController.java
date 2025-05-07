package org.xue.assistant.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.assistant.chat.entity.FileInfo;
import org.xue.assistant.chat.service.FileService;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    // 分页查询文件列表
    @GetMapping("/list")
    public Page<FileInfo> listFiles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return fileService.listFiles(page, size);
    }

    @PostMapping("/upload")
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileInfo fileInfo = fileService.uploadAndParseFile(file);
            return ResponseEntity.ok(fileInfo);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRecord(@PathVariable String id) {
        fileService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }

}