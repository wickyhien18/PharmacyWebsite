package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "inventories")

//Create object easily
// ClassName.builder().atribute1().attribute2.build()
// Produces complex builder APIs for this class via Lombok.
@Builder

//Create constructor no args
// Generates a no-argument constructor via Lombok.
@NoArgsConstructor

//Create constructor with all args
// Generates an all-arguments constructor via Lombok.
@AllArgsConstructor

//Generate Getter method for all attributes
@Getter
//Generate Setter method for all attributes
@Setter
/**
 * Database Entity for Inventory.
 * This class is used to map data and handle basic structure.
 */
public class Inventory {

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "inventory_id")
    private Long inventoryId ;

    // Specifies a single-valued association to another entity that has one-to-one multiplicity.
    @OneToOne(fetch = FetchType.LAZY)
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "medicine_id", nullable = false, unique = true)
    private Medicines medicines;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false)
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private Integer quantity = 0;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
