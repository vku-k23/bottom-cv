package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dayOfBirth;

    private String address;

    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String avatar;

    private String description;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Profile profile = (Profile) obj;
        return Objects.equals(id, profile.id) &&
                Objects.equals(firstName, profile.firstName) &&
                Objects.equals(lastName, profile.lastName) &&
                Objects.equals(email, profile.email);
    }
}
