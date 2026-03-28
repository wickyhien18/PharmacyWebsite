package com.example.Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
public class CartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int Id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Carts carts;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;

    private int quantity;
}
