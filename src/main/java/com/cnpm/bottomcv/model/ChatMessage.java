package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "chat_messages")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 255)
    private String conversationId;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // USER or AI

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
}

