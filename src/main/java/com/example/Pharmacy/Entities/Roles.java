package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int role_id;

    @Column(name = "role_name", nullable = false)
    private String role_name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "roles")
    @JsonIgnore
    private List<Users> users;
}
