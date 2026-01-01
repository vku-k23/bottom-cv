package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.ApplicationStatus;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "application_status_history")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private Apply application;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private ApplicationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ApplicationStatus newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "offer_details", columnDefinition = "TEXT")
    private String offerDetails; // JSON for storing offer info (salary, start_date, etc.)
}
