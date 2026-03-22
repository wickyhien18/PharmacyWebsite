package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "manufacturers")
@Getter
@Setter
public class Manufacturers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_id")
    private int manufacturer_id;

    @Column(nullable = false, name = "manufacturer_name")
    private String manufacturer_name;

    private String country;

    @OneToMany(mappedBy = "manufacturers")
    @JsonIgnore
    private List<Medicines> medicines;
}
