package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true, exclude = {"users"})
@Data
@Table(name = "roles")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleType name;

    @ManyToMany(mappedBy = "roles")
    @JsonBackReference
    @ToString.Exclude
    @Builder.Default
    private Set<User> users = new HashSet<>();
}