package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Introduce is required")
    @Size(max = 500, message = "Introduce must be less than 500 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s.,;:!?'-]+$", message = "Introduce contains invalid characters")
    private String introduce;

    private Map<String, String> socialMediaLinks;

    private Map<String, String> addresses;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain only digits and be between 10-15 characters")
    private String phone;

    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email contains invalid characters")
    private String email;

    private String website;

    @NotBlank(message = "Logo is required")
    private String logo;

    private String cover;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotBlank(message = "Company size is required")
    private String companySize;

    @NotNull(message = "Founded year is required")
    @Min(value = 1800, message = "Founded year must be after 1800")
    private Integer foundedYear;
}
