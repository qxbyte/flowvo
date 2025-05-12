package org.xue.core.chat.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xue.api.dto.request.milvus.InsertChunksRequest;
import org.xue.api.dto.request.milvus.SearchChunksRequest;
import org.xue.core.chat.entity.FileInfo;
import org.xue.core.chat.repository.FileInfoRepository;
import org.xue.core.chat.service.FileService;
import org.xue.core.chat.util.ContentUtils;
import org.xue.core.client.feign.MilvusFeign;
import org.xue.core.milvus.service.ChunkMilvusService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileInfoRepository fileInfoRepository;
    private final ChunkMilvusService milvusService;
    private final MilvusFeign milvusFeign;

    public FileServiceImpl(FileInfoRepository fileInfoRepository, ChunkMilvusService milvusService, MilvusFeign milvusFeign) {
        this.fileInfoRepository = fileInfoRepository;
        this.milvusService = milvusService;
        this.milvusFeign = milvusFeign;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
//        milvusService.insertChunks(content, fileInfo.getId());
        milvusFeign.insertChunks(new InsertChunksRequest(content, fileInfo.getId()));

        return fileInfo; // 不在这里保存，由调用者设置userId后保存
    }

    // 分页查询文件列表
    @Override
    public Page<FileInfo> listFiles(int page, int size) {
        return fileInfoRepository.findAll(PageRequest.of(page, size));
    }
    
    @Override
    public Page<FileInfo> listFilesByUserId(Long userId, int page, int size) {
        return fileInfoRepository.findByUserIdOrderByUploadTimeDesc(userId, PageRequest.of(page, size));
    }
    
    @Override
    public FileInfo getFileById(String id) {
        return fileInfoRepository.findById(id).orElse(null);
    }
    
    @Override
    public FileInfo saveFileInfo(FileInfo fileInfo) {
        return fileInfoRepository.save(fileInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(String id) {
        fileInfoRepository.deleteById(id);
//        milvusService.deleteById(id);
        milvusFeign.deleteById(id);
    }
}
