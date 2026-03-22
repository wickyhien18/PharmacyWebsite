package com.example.Pharmacy.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderItemId implements Serializable {

    @Column(name = "order_id")
    private int order_id;
    @Column(name = "medicine_id")
    private int medicine_id;

    public OrderItemId() {}

    public OrderItemId(int order_id, int medicine_id) {
        this.order_id = order_id;
        this.medicine_id = medicine_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getMedicine_id() {
        return medicine_id;
    }

    public void setMedicine_id(int medicine_id) {
        this.medicine_id = medicine_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)  return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemId that = (OrderItemId) o;
        return Objects.equals(order_id, that.order_id) && Objects.equals(medicine_id, that.medicine_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order_id, medicine_id);
    }
}
