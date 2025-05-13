package org.xue.core.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.core.chat.entity.ChatRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRecordRepository extends JpaRepository<ChatRecord, String> {
    List<ChatRecord> findByUserIdOrderByUpdateTimeDesc(String userId);
    
    List<ChatRecord> findByUserIdAndTypeOrderByUpdateTimeDesc(String userId, String type);
    
    Optional<ChatRecord> findFirstByUserIdAndTypeOrderByUpdateTimeDesc(String userId, String type);
    
    Optional<ChatRecord> findByIdAndUserId(String id, String userId);
    
    Optional<ChatRecord> findByUserIdAndType(String userId, String type);

    @Query("UPDATE ChatRecord c SET c.title = :title WHERE c.id = :id")
    @Modifying
    void updateTitle(@Param("id") String id, @Param("title") String title);

    @Query("SELECT m FROM ChatRecord m ORDER BY m.createTime ASC")
    List<ChatRecord> findAllOrderByCreateTime();
}


