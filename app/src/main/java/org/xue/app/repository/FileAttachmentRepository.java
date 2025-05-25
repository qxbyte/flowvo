package org.xue.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xue.app.entity.FileAttachment;

import java.util.List;

/**
 * 文件附件数据访问层
 */
@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, String> {
    
    /**
     * 根据对话ID查找所有附件
     *
     * @param conversationId 对话ID
     * @return 附件列表
     */
    List<FileAttachment> findByConversationId(String conversationId);
    
    /**
     * 根据用户ID查找所有附件
     *
     * @param userId 用户ID
     * @return 附件列表
     */
    List<FileAttachment> findByUserId(String userId);
    
    /**
     * 根据对话ID和用户ID查找附件
     *
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @return 附件列表
     */
    List<FileAttachment> findByConversationIdAndUserId(String conversationId, String userId);
} 