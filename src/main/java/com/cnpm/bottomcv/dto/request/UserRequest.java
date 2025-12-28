package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.validation.InvalidWords.InvalidWords;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest extends RegisterUserDto {

    private String avatar;

    @InvalidWords(message = "Description contains invalid words")
    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @NotNull(message = "Roles is required")
    private Set<String> roles = new HashSet<>();
}
