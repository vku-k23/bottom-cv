package com.cnpm.bottomcv.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFilterRequest {
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "id";
    private String sortType = "asc";
    private String search;
    private String industry;
    private Boolean verified;
}

