package com.example.Pharmacy.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Cleanup;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MediCateId implements Serializable {

    @Column(name = "medicine_id")
    private int medicine_id;
    @Column(name = "category_id")
    private int category_id;

    public MediCateId() {}
    public MediCateId(int mid, int cid) {
        this.medicine_id = mid;
        this.category_id = cid;
    }

    public int getMedicine_id() {
        return medicine_id;
    }

    public void setMedicine_id(int medicine_id) {
        this.medicine_id = medicine_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediCateId that = (MediCateId) o;
        return Objects.equals( medicine_id,that.medicine_id) && Objects.equals(category_id, that.category_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicine_id, category_id);
    }
}
