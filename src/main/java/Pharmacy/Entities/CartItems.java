package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "cart_item",
    uniqueConstraints = @UniqueConstraint(
            name = "uk_cart_medicine",
            columnNames = {"cart_id", "medicine_id"}
    ))

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
public class CartItems {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "cart_item_id")
    private Long cartItemId;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "cart_id")
    private Carts carts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}
