package org.xue.aibot.repository;

import org.xue.aibot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatId(Long chatId);

    void deleteByChatId(Long chatId);  // 添加这个方法用于删除与某个对话记录相关的所有消息

}



