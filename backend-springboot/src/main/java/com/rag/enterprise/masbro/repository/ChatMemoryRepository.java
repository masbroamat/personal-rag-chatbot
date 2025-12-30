package com.rag.enterprise.masbro.repository;

import com.rag.enterprise.masbro.entity.ChatMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemoryRepository extends JpaRepository<ChatMemoryEntity, String> {
    void deleteByChatId(String chatId);
}