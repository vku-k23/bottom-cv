package com.cnpm.bottomcv.model;
import com.cnpm.bottomcv.constant.StatusVerificationToken;
import com.cnpm.bottomcv.constant.TypeVerificationToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationToken implements Serializable {
    private String token;
    private String email;
    private String phoneNumber;
    private TypeVerificationToken type;
    private StatusVerificationToken status;
}