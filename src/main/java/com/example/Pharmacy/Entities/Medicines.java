package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "medicines")

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
public class Medicines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private int medicine_id;

    @Column(nullable = false, name = "medicine_name")
    private String medicine_name;

    @Column(name = "medicine_image")
    private String medicine_image;
    private String description;
    private float price;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturers manufacturers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Categories categories;

    @CreationTimestamp
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "medicines")
    @JsonIgnore
    private List<CartItems> cartItems;

    @OneToMany(mappedBy = "medicines")
    @JsonIgnore
    private List<OrderItems> orderItems;
}
