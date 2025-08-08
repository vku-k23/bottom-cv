package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "payments")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String provider; // e.g., STRIPE, VNPAY

    @Column(nullable = false)
    private String status; // e.g., CREATED, PAID, FAILED

    @Column
    private String referenceId; // provider session/txn id

    @Column
    private String currency;

    @Column
    private Long amountMinor; // amount in minor units
}