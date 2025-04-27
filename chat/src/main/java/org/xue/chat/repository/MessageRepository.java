package org.xue.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.chat.entity.Messages;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Messages, String> {
    List<Messages> findByChatIdOrderByCreateTimeAsc(String chatId);

    @Query("SELECT m FROM Messages m WHERE m.chatId = :chatId AND m.role = 'user' ORDER BY m.createTime ASC LIMIT 1")
    Optional<Messages> findFirstUserMessage(@Param("chatId") String chatId);
}



