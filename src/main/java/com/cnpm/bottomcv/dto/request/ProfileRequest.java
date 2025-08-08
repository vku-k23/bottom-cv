package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.validation.InvalidWords.InvalidWords;
import com.cnpm.bottomcv.validation.PastDateOfBirth.PastDateOfBirth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 30, message = "First name must be between 3 and 30 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 30, message = "Last name must be between 3 and 30 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @PastDateOfBirth(message = "Date of birth must be in the past")
    private LocalDate dayOfBirth;

    @Size(max = 255, message = "Address must be less than 255 characters")
    @InvalidWords(message = "Address contains invalid words")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain only digits and be between 10-15 characters")
    private String phoneNumber;

    private String avatar;

    @Size(max = 500, message = "Description must be less than 500 characters")
    @InvalidWords(message = "Description contains invalid words")
    private String description;
}
