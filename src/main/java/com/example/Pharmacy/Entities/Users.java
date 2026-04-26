package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.example.Pharmacy.Entities.Roles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "users")

//Create object easily
// ClassName.builder().atribute1().attribute2.build()
@Builder

//Create constructor no args
@NoArgsConstructor

//Create constructor with all args
@AllArgsConstructor

//Generate Getter method for all attributes
@Getter
//Generate Setter method for all attributes
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private int userId;

    @Column(nullable = false, unique = true, name = "user_name")
    private String userName;

    private String password;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    @CreationTimestamp
    private LocalDateTime last_activity;

    private boolean is_active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Roles roles;

    @OneToOne(mappedBy = "users")
    @JsonIgnore
    private Carts carts;

    @OneToMany(mappedBy = "users")
    @JsonIgnore
    private List<Orders> orders;
}
