package org.xue.app.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xue.app.entity.Conversation;

/**
 * 对话数据访问层
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    /**
     * 根据服务名称查找对话并分页
     *
     * @param service 服务名称
     * @param pageable 分页参数
     * @return 分页对话结果
     */
    Page<Conversation> findByService(String service, Pageable pageable);
    
    /**
     * 根据标题查找对话并分页
     *
     * @param title 标题
     * @param pageable 分页参数
     * @return 分页对话结果
     */
    Page<Conversation> findByTitleContaining(String title, Pageable pageable);
    
    /**
     * 根据来源查找对话，按创建时间降序排序
     *
     * @param source 对话来源
     * @return 对话列表
     */
    List<Conversation> findBySourceOrderByCreatedAtDesc(String source);
} 