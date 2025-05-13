package org.xue.core.chat.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.core.chat.entity.FileInfo;
import org.xue.core.entity.User;
import org.xue.core.chat.service.FileService;
import org.xue.core.service.UserService;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    private final UserService userService;

    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("未授权的访问");
        }
        
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    // 分页查询文件列表
    @GetMapping("/list")
    public Page<FileInfo> listFiles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        User user = getCurrentUser();
        // 只返回当前用户的文件
        return fileService.listFilesByUserId(user.getId(), page, size);
    }

    @PostMapping("/upload")
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            User user = getCurrentUser();
            FileInfo fileInfo = fileService.uploadAndParseFile(file);
            // 设置文件所有者
            fileInfo.setUserId(user.getId());
            fileInfo = fileService.saveFileInfo(fileInfo);
            return ResponseEntity.ok(fileInfo);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        User user = getCurrentUser();
        // 验证文件所有权
        FileInfo fileInfo = fileService.getFileById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        fileService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }
}