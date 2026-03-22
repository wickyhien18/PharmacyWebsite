package com.example.Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medicine_category")
@Getter
@Setter
public class MedicineCategories {

    @EmbeddedId
    private MediCateId mediCateId;

    @ManyToOne
    @MapsId("medicine_id")
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;

    @ManyToOne
    @MapsId("category_id")
    @JoinColumn(name = "category_id")
    private Categories categories;

}
