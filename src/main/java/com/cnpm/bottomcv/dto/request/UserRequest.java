package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest extends RegisterUserDto {
    @NotNull(message = "Roles is required")
    private Set<Role> roles = new HashSet<>();
}
