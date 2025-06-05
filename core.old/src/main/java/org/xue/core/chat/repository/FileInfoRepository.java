package org.xue.core.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xue.core.chat.entity.FileInfo;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, String> {
    // 继承JpaRepository即可拥有分页功能
    
    /**
     * 根据用户ID分页查询文件
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<FileInfo> findByUserIdOrderByUploadTimeDesc(Long userId, Pageable pageable);
}