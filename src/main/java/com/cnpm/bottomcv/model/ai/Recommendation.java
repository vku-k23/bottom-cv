package com.cnpm.bottomcv.model.ai;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Getter
@Setter
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_ids", nullable = false, columnDefinition = "JSON")
    private String jobIds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}