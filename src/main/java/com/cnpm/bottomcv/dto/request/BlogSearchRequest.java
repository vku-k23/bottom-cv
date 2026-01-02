package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogSearchRequest {

    private String keyword;
    private BlogStatus status;
    private Long categoryId;
    private Long authorId;
    
    private String sortBy;
    private String sortDirection;
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 10;
}


