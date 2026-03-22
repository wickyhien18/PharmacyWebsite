package com.example.Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
public class Carts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private int cart_id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @CreationTimestamp
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "carts")
    @JsonIgnore
    private List<CartItems> cartItems;

}
