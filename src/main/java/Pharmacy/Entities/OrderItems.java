package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "order_items")

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
 * Database Entity for OrderItems.
 * This class is used to map data and handle basic structure.
 */
public class OrderItems {

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "order_item_id")
    private Long orderItemId;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false)
    private Integer quantity;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    //1 - N Relationship
    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;

    // Snapshot of price at time of booking — price may change later
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;
}
