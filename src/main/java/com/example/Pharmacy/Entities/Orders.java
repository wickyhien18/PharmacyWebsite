package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.orm.jpa.persistenceunit.SpringPersistenceUnitInfo;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int order_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    private float total_price;
    private String status;

    @CreationTimestamp
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "orders")
    @JsonIgnore
    private List<OrderItems> orderItems;

    @OneToOne(mappedBy = "orders")
    @JsonIgnore
    private Payments payments;
}
