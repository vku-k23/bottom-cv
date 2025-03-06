package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Introduce is required")
    private String introduce;

    private Map<String, String> socialMediaLinks;

    private Map<String, String> addresses;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain only digits and be between 10-15 characters")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String website;

    @NotBlank(message = "Logo is required")
    private String logo;

    private String cover;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotBlank(message = "Company size is required")
    private String companySize;

    @NotBlank(message = "Founded year is required")
    private Integer foundedYear;
}
