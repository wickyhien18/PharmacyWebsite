package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "inventory")

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
public class Inventory {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "inventory_id")
    private Long inventoryId ;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;

    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
