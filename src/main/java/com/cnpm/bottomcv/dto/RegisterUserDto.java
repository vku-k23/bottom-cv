package com.cnpm.bottomcv.dto;

import com.cnpm.bottomcv.validation.PastDateOfBirth.PastDateOfBirth;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterUserDto {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
             message = "Password must contain like Az0@1 and be at least 6 characters long")
    @Size(min = 4, max = 50, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain only digits and be between 10-15 characters")
    private String phoneNumber;

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 25, message = "First name must between 3 and 25 character")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 25, message = "Last name must between 3 and 25 character")
    private String lastName;

    @NotBlank(message = "Date of birth is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @PastDateOfBirth(message = "Date of birth must be in the past")
    private LocalDate dayOfBirth;

}