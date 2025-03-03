package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String dayOfBirth;
    private String address;
    private String phoneNumber;
    private String email;
    private String avatar;
    private String description;
}
