package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity to store saved candidates by employers.
 * An employer can save candidates who have applied to their jobs.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "saved_candidates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employer_id", "candidate_id", "job_id"})
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employer who saved this candidate
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "employer_id")
    private User employer;

    /**
     * The candidate (user) who was saved
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    /**
     * The job the candidate applied to (optional, for context)
     */
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    /**
     * Optional note about why this candidate was saved
     */
    @Column(columnDefinition = "TEXT")
    private String note;
}

