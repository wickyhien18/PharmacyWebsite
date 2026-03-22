package com.example.Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
public class CartItems {

    @EmbeddedId
    private CartItemId cartItemId;

    @ManyToOne
    @MapsId("cart_id")
    @JoinColumn(name = "cart_id")
    private Carts carts;

    @ManyToOne
    @MapsId("medicine_id")
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;

    private int quantity;
}
