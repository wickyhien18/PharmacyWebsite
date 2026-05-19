package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "medicines")

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
 * Database Entity for Medicines.
 * This class is used to map data and handle basic structure.
 */
public class Medicines {

    public enum Status{ACTIVE, INACTIVE, OUT_OF_STOCK}

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "medicine_id")
    private Long medicineId;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, name = "name", length = 500)
    private String medicineName;

    // Specifies the mapped column for a persistent property or field.
    @Column(unique = true, nullable = false, length = 500, name = "slug")
    private String medicineSlug;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "image", length = 500)
    private String medicineImage;

    // Specifies the mapped column for a persistent property or field.
    @Column(columnDefinition = "TEXT")
    private String description;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // Specifies the mapped column for a persistent property or field.
    @Column(length = 50)
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private String unit = "Pillbox";

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "manufacturer_id")
    private Manufacturers manufacturers;

    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "category_id")
    private Categories categories;

    @Enumerated(EnumType.STRING)
    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false)
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private Status status = Status.ACTIVE;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "expire_date")
    private LocalDate expireDate;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    /**
     * On create.
     */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    /**
     * On update.
     */
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
