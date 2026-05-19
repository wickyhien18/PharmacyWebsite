package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "inventory_logs")

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
 * Database Entity for InventoryLog.
 * This class is used to map data and handle basic structure.
 */
public class InventoryLog {

    public enum ChangeType { IMPORT, EXPORT, ADJUST }

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "log_id")
    private Long logId;

    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicines medicines;

    @Enumerated(EnumType.STRING)
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false)
    private Integer quantity;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    // order_id if sales, null if manual inventory
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "reference_id")
    private Long referenceId;

    // Specifies the mapped column for a persistent property or field.
    @Column(length = 500)
    private String note;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    /**
     * On create.
     */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}