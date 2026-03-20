package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int medicine_id;

    @Column(nullable = false)
    private String medicine_name;

    private String medicine_image;
    private String description;
    private float price;
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturers manufacturer;

    @OneToMany(mappedBy = "medicines")
    @JsonIgnore
    private List<MedicineCategories> medicineCategories;

    @CreationTimestamp
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "medicines")
    @JsonIgnore
    private List<CartItems> cartItems;

    @OneToMany(mappedBy = "medicines")
    @JsonIgnore
    private List<OrderItems> orderItems;
}
