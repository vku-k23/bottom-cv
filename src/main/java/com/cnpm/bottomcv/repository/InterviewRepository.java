package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByJobId(Long jobId);

    List<Interview> findByCandidateId(Long candidateId);
}