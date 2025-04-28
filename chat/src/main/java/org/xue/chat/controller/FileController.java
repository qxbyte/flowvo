package org.xue.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.chat.entity.FileInfo;
import org.xue.chat.service.FileService;

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
}