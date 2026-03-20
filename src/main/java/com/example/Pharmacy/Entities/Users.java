package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.example.Pharmacy.Entities.Roles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int user_id;

    @Column(nullable = false)
    private String user_name;

    private String password;

    @CreationTimestamp
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;

    @OneToOne(mappedBy = "users")
    private Carts cart;

    @OneToMany(mappedBy = "users")
    @JsonIgnore
    private List<Orders> orders;
}
