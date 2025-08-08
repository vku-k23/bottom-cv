package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(Long senderId, Long recipientId);
}