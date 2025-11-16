package com.cnpm.bottomcv.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobFilterRequest {
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "id";
    private String sortType = "asc";
    private String search;
    private String jobType;
    private String location;
    private String status;
    private Long companyId;
    private Long categoryId;
}

