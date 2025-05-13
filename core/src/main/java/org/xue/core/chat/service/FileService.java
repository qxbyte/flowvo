package org.xue.core.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xue.core.chat.entity.FileInfo;
import java.io.IOException;

@Service
public interface FileService {

    @Transactional
    FileInfo uploadAndParseFile(MultipartFile file) throws IOException;

    // 分页查询文件列表
    Page<FileInfo> listFiles(int page, int size);
    
    /**
     * 根据用户ID分页查询文件列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 文件分页列表
     */
    Page<FileInfo> listFilesByUserId(Long userId, int page, int size);
    
    /**
     * 根据ID获取文件信息
     * @param id 文件ID
     * @return 文件信息
     */
    FileInfo getFileById(String id);
    
    /**
     * 保存文件信息
     * @param fileInfo 文件信息
     * @return 保存后的文件信息
     */
    FileInfo saveFileInfo(FileInfo fileInfo);

    @Transactional
    void deleteDocument(String id);
}