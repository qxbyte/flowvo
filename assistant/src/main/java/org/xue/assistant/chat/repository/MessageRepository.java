package org.xue.assistant.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xue.assistant.chat.entity.Messages;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Messages, String> {
    List<Messages> findByChatIdOrderByCreateTimeAsc(String chatId);

    @Query("SELECT m FROM Messages m WHERE m.chatId = :chatId AND m.role = 'user' ORDER BY m.createTime ASC LIMIT 1")
    Optional<Messages> findFirstUserMessage(@Param("chatId") String chatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Messages m WHERE m.chatId = :chatId")
    void deleteByChatId(@Param("chatId") String chatId);
}



