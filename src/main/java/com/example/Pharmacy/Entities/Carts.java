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
@Table(name = "carts")

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
public class Carts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private int cart_id;

    @OneToOne
    @JoinColumn(name = "userId")
    private Users users;

    @CreationTimestamp
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "carts")
    @JsonIgnore
    private List<CartItems> cartItems;

}
