package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerificationQueueResponse {
    private Long id;
    private String name;
    private String slug;
    private String industry;
    private String companySize;
    private Integer foundedYear;
    private String email;
    private String phone;
    private String website;
    private String logo;
    private Boolean verified;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED
    private String submittedAt;
    private Integer jobCount;
    private String notes; // admin notes
}

