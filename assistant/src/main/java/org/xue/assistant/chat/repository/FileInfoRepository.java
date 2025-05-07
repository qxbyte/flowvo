package org.xue.assistant.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xue.assistant.chat.entity.FileInfo;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, String> {
    // 继承JpaRepository即可拥有分页功能
}