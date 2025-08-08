package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.InterviewStatus;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "interviews")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column
    private String location; // or meeting link

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;
}