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
    @JoinColumn(name = "cv_id", nullable = false)
    private CV cv;

    private StatusJob status;

    private String message;
}
