package org.xue.assistant.functioncall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.xue.assistant.functioncall.entity.CallMessage;

import java.util.List;

public interface CallMessageRepository extends JpaRepository<CallMessage, Long> {

    List<CallMessage> findByChatIdOrderByCreatedAtAsc(Long chatId);

    void deleteByChatId(Long chatId);
}

