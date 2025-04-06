package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "companies")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String introduce;

    @ElementCollection
    private Map<String, String> socialMediaLinks;

    @ElementCollection
    private Map<String, String> addresses;

    private String phone;

    private String email;

    private String website;

    private String logo;

    private String cover;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false)
    private String companySize;

    private Integer foundedYear;

    @OneToMany(mappedBy = "company")
    private Set<Job> jobs = new HashSet<>();

    @OneToMany(mappedBy = "company")
    private Set<Review> reviews = new HashSet<>();;
}
