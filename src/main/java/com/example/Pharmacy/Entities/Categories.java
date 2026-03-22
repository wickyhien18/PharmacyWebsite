package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int category_id;

    @Column(nullable = false, name = "category_name")
    private String category_name;

    private String description;

    @OneToMany(mappedBy = "categories")
    @JsonIgnore
    private List<MedicineCategories> medicineCategories;
}
