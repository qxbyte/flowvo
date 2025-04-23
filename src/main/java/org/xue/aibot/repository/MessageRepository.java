package org.xue.aibot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.aibot.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Messages, String> {
    List<Messages> findByChatRecordIdOrderByCreateTimeAsc(String chatId);

    @Query("SELECT m FROM Messages m WHERE m.chatRecord.id = :chatId AND m.role = 'user' ORDER BY m.createTime ASC LIMIT 1")
    Optional<Messages> findFirstUserMessage(@Param("chatId") String chatId);
}



