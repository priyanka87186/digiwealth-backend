package com.digiwealth.ai.repository;

import com.digiwealth.ai.entity.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {
    List<AiChat> findByUserIdOrderByCreatedAtDesc(Long userId);
}
