package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.example.Pharmacy.Entities.Roles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Getter
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int user_id;

    @Column(nullable = false, unique = true, name = "user_name")
    private String user_name;

    @JsonIgnore
    private String password;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

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
