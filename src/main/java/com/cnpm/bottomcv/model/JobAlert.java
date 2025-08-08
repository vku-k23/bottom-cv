package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.AlertFrequency;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "job_alerts")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String keywords;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertFrequency frequency;

    @Column(nullable = false)
    private boolean enabled;
}