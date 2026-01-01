package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "applies")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apply extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;

    @Column(name = "cv_url")
    private String cvUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusJob status; // Keep for backward compatibility

    @ManyToOne
    @JoinColumn(name = "status_column_id")
    private StatusColumn statusColumn; // Reference to status column

    @Column(name = "position")
    private Integer position; // Position/order within the status column (0-based)

    private String message;
}
