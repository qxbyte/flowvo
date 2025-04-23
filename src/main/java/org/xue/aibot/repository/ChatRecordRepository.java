package org.xue.aibot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.aibot.entity.ChatRecord;
import java.util.Optional;

import java.util.List;

@Repository
public interface ChatRecordRepository extends JpaRepository<ChatRecord, String> {
    List<ChatRecord> findByUserIdOrderByUpdateTimeDesc(String userId);

    Optional<ChatRecord> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE ChatRecord c SET c.title = :title WHERE c.id = :id")
    void updateTitle(@Param("id") String id, @Param("title") String title);
}


