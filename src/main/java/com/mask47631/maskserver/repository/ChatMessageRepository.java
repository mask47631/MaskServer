package com.mask47631.maskserver.repository;

import com.mask47631.maskserver.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.id > :id ORDER BY cm.id ASC")
    List<ChatMessage> findByIdGreaterThanOrderByIdAsc(@Param("id") Long id, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.id > :id ORDER BY cm.id DESC")
    List<ChatMessage> findByIdGreaterThanOrderByIdDesc(@Param("id") Long id, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.id < :id ORDER BY cm.id DESC")
    List<ChatMessage> findByIdLessThanOrderByIdDesc(@Param("id") Long id, Pageable pageable);
}