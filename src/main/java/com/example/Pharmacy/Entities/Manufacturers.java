package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "manufacturers")
public class Manufacturers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int manufacturer_id;

    @Column(nullable = false)
    private String manufacturer_name;

    private String country;

    @OneToMany(mappedBy = "manufacturers")
    @JsonIgnore
    private List<Medicines> medicines;
}
