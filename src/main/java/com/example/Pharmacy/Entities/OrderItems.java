package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItems {

    @EmbeddedId
    private OrderItemId orderItemId;

    private int quantity;
    private float price;

    @ManyToOne
    @MapsId("order_id")
    @JoinColumn(name = "order_id")
    private Orders orders;

    @ManyToOne
    @MapsId("medicine_id")
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;
}
