package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "status_columns")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusColumn extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // Display name of the column

    @Column(nullable = false, length = 50, unique = true)
    private String code; // Unique code (e.g., "PENDING", "ACTIVE", "CUSTOM_1")

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder; // Order for display in UI

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false; // Whether this is a default system column

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = true)
    private Job job; // If null, it's a global column; if set, it's job-specific

    @OneToMany(mappedBy = "statusColumn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Apply> applications = new ArrayList<>();
}

