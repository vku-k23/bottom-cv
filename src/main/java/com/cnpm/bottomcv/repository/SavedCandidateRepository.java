package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.SavedCandidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedCandidateRepository extends JpaRepository<SavedCandidate, Long> {

    /**
     * Find all saved candidates by employer ID with pagination
     */
    Page<SavedCandidate> findByEmployerId(Long employerId, Pageable pageable);

    /**
     * Find all saved candidates by employer ID
     */
    List<SavedCandidate> findByEmployerId(Long employerId);

    /**
     * Check if a candidate is already saved by an employer for a specific job
     */
    Optional<SavedCandidate> findByEmployerIdAndCandidateIdAndJobId(
            Long employerId, Long candidateId, Long jobId);

    /**
     * Check if a candidate is already saved by an employer (regardless of job)
     */
    Optional<SavedCandidate> findByEmployerIdAndCandidateId(Long employerId, Long candidateId);

    /**
     * Count saved candidates by employer
     */
    Long countByEmployerId(Long employerId);

    /**
     * Delete saved candidate by employer, candidate, and job
     */
    void deleteByEmployerIdAndCandidateIdAndJobId(Long employerId, Long candidateId, Long jobId);

    /**
     * Check if exists by employer, candidate, and job
     */
    boolean existsByEmployerIdAndCandidateIdAndJobId(Long employerId, Long candidateId, Long jobId);

    /**
     * Find saved candidates by employer with candidate profile info
     */
    @Query("SELECT sc FROM SavedCandidate sc " +
           "LEFT JOIN FETCH sc.candidate c " +
           "LEFT JOIN FETCH c.profile " +
           "LEFT JOIN FETCH sc.job j " +
           "WHERE sc.employer.id = :employerId")
    List<SavedCandidate> findByEmployerIdWithDetails(@Param("employerId") Long employerId);
}

