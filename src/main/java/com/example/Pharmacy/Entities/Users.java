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

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "userId")
    private int userId;

    @Column(nullable = false, unique = true, name = "user_name")
    private String userName;

    private String password;

    //Default value is right now
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    @CreationTimestamp
    private LocalDateTime last_activity;

    private boolean is_active;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "role_id")
    private Roles roles;

    //1 - 1 Relationship
    //Mapped by another entities
    @OneToOne(mappedBy = "users")

    //Avoid infinite loop
    @JsonIgnore
    private Carts carts;

    //1 - N Relationship
    @OneToMany(mappedBy = "users")
    @JsonIgnore
    private List<Orders> orders;
}
