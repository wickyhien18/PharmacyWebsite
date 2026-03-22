package com.example.Pharmacy.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CartItemId implements Serializable {

    @Column(name = "cart_id")
    private int cart_id;
    @Column(name = "medicine_id")
    private int medicine_id;

    public CartItemId() {}

    public CartItemId(int cart_id, int medicine_id) {
        this.cart_id = cart_id;
        this.medicine_id = medicine_id;
    }

    public int getCart_id() {
        return cart_id;
    }

    public void setCart_id(int cart_id) {
        this.cart_id = cart_id;
    }

    public int getMedicine_id() {
        return medicine_id;
    }

    public void setMedicine_id(int medicine_id) {
        this.medicine_id = medicine_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemId that = (CartItemId) o;
        return Objects.equals(cart_id,that.cart_id) && Objects.equals(medicine_id,that.medicine_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cart_id, medicine_id);
    }
}
