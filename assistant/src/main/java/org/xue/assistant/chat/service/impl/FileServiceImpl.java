package org.xue.assistant.chat.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xue.assistant.chat.entity.FileInfo;
import org.xue.assistant.chat.repository.FileInfoRepository;
import org.xue.assistant.chat.service.FileService;
import org.xue.assistant.chat.util.ContentUtils;
import org.xue.assistant.milvus.service.ChunkMilvusService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileInfoRepository fileInfoRepository;
    private final ChunkMilvusService milvusService;

    public FileServiceImpl(FileInfoRepository fileInfoRepository, ChunkMilvusService milvusService) {
        this.fileInfoRepository = fileInfoRepository;
        this.milvusService = milvusService;
    }

    @Transactional
    public FileInfo uploadAndParseFile(MultipartFile file) throws IOException {
        // 获取文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename;
        String fileExtension = "";

        int lastDotIndex = originalFilename != null ? originalFilename.lastIndexOf(".") : -1;
        if (lastDotIndex > 0) {
            fileName = originalFilename.substring(0, lastDotIndex);
            fileExtension = originalFilename.substring(lastDotIndex + 1);
        }

        // 读取文件内容，保留换行格式
        String content = ContentUtils.readFileContent(file);

        // 创建并保存文件信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(UUID.randomUUID().toString());
        fileInfo.setFileName(fileName);
        fileInfo.setFileExtension(fileExtension);
        fileInfo.setUploadTime(LocalDateTime.now());

        //保存向量数据库
        milvusService.insertChunks(content, fileInfo.getId());

        return fileInfoRepository.save(fileInfo);
    }

    // 分页查询文件列表
    public Page<FileInfo> listFiles(int page, int size) {
        return fileInfoRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    public void deleteDocument(String id) {
        fileInfoRepository.deleteById(id);
        milvusService.deleteById(id);
    }
}
