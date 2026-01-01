package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.StatusJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private Long cvId;
    private String cvUrl;
    private String coverLetter;
    private StatusJob status;
    private Integer position; // Position/order within the status column
    private String message;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // Candidate profile information (included for employer/admin viewing)
    private CandidateProfile candidateProfile;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CandidateProfile {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String address;
        private String avatar;
        private String dayOfBirth;
        private String description;
    }
}