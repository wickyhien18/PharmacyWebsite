package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "categories")
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int category_id;

    @Column(nullable = false)
    private String category_name;

    private String description;

    @OneToMany(mappedBy = "categories")
    @JsonIgnore
    private List<MedicineCategories> medicineCategories;
}
