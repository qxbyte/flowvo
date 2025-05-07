package org.xue.assistant.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xue.assistant.chat.entity.FileInfo;
import java.io.IOException;

@Service
public interface FileService {

    @Transactional
    FileInfo uploadAndParseFile(MultipartFile file) throws IOException;

    // 分页查询文件列表
    Page<FileInfo> listFiles(int page, int size);

    @Transactional
    void deleteDocument(String id);
}