package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "reports")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(nullable = false)
    private String resourceType; // JOB, REVIEW, COMPANY, USER

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private boolean resolved;
}