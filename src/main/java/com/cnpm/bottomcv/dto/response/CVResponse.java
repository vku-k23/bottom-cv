package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVResponse {
    private Long id;
    private Long userId;
    private String title;
    private String cvFile;
}
