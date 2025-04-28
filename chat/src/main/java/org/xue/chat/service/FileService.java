package org.xue.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.chat.entity.FileInfo;
import org.xue.chat.repository.FileInfoRepository;
import org.xue.chat.util.ContentUtils;
import org.xue.milvus.service.ChunkMilvusService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final ChunkMilvusService milvusService;

    public FileService(FileInfoRepository fileInfoRepository, ChunkMilvusService milvusService) {
        this.fileInfoRepository = fileInfoRepository;
        this.milvusService = milvusService;
    }

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
}