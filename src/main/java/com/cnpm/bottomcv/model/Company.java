package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
