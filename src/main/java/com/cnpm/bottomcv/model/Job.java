package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "jobs")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String jobDescription;

    @Column(nullable = false)
    private String jobRequirement;

    @Column(nullable = false)
    private String jobBenefit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    @Column(nullable = false)
    private String location;

    private String workTime;

    private Double salary;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private StatusJob status;

    @ManyToMany
    @JoinTable(
            name = "job_category",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}