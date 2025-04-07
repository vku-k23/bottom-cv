package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "cvs")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CV extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String cvFile;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private StatusJob status = StatusJob.ACTIVE;

    private String skills;

    private String experience;

    private String content;
}
