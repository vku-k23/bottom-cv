package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyResponse {
    private Long id;

    private String name;

    private String slug;

    private String introduce;

    private Map<String, String> socialMediaLinks;

    private Map<String, String> addresses;

    private String phone;

    private String email;

    private String website;

    private String logo;

    private String cover;

    private String industry;

    private String companySize;

    private Integer foundedYear;

    private List<JobResponse> jobs;
}

