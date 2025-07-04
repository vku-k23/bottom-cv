package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String slug;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

}
