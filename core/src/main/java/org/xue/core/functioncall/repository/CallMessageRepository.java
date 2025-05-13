package org.xue.core.functioncall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.xue.core.functioncall.entity.CallMessage;

import java.util.List;

public interface CallMessageRepository extends JpaRepository<CallMessage, Long> {
    // 根据chatId查询所有消息，按创建时间排序
    List<CallMessage> findByChatIdOrderByCreatedAtAsc(String chatId);
}

