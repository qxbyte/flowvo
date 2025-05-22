package org.xue.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.app.entity.ChatMessage;

import java.util.List;
import java.util.Optional;

/**
 * 聊天消息数据访问层
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    
    /**
     * 根据对话ID查找消息并分页
     *
     * @param conversationId 对话ID
     * @param pageable 分页参数
     * @return 分页消息结果
     */
    Page<ChatMessage> findByConversationId(String conversationId, Pageable pageable);
    
    /**
     * 根据对话ID查找所有消息并按序号排序
     *
     * @param conversationId 对话ID
     * @return 消息列表
     */
    List<ChatMessage> findByConversationIdOrderBySequenceAsc(String conversationId);
    
    /**
     * 查找对话中的最后一条消息
     *
     * @param conversationId 对话ID
     * @return 最后一条消息
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversationId = :conversationId ORDER BY m.sequence DESC")
    Page<ChatMessage> findLastMessageByConversationId(@Param("conversationId") String conversationId, Pageable pageable);
    
    /**
     * 查找指定角色的最后一条消息
     *
     * @param conversationId 对话ID
     * @param role 角色
     * @return 最后一条消息
     */
    Optional<ChatMessage> findTopByConversationIdAndRoleOrderBySequenceDesc(String conversationId, String role);
    
    /**
     * 获取对话中的最大序号
     *
     * @param conversationId 对话ID
     * @return 最大序号
     */
    @Query("SELECT MAX(m.sequence) FROM ChatMessage m WHERE m.conversationId = :conversationId")
    Integer findMaxSequenceByConversationId(@Param("conversationId") String conversationId);
} 