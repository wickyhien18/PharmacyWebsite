package com.example.Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private int payment_id;

    @Column(name = "payment_method")
    private String payment_method;

    private float amount;
    private String status;

    @CreationTimestamp
    private LocalDateTime created_at;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders orders;
}
