package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "carts")

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
 * Database Entity for Carts.
 * This class is used to map data and handle basic structure.
 */
public class Carts {

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "cart_id")
    private Long cartId;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity that has one-to-one multiplicity.
    @OneToOne(fetch = FetchType.LAZY)

    //Foreign Key
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "user_id")
    private Users users;

    //Default value is right now
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //1 - N Relationship
    //Mapped by another Entities
    // Specifies a many-valued association with one-to-many multiplicity.
    @OneToMany(mappedBy = "carts", cascade = CascadeType.ALL, orphanRemoval = true)
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private List<CartItems> cartItems = new ArrayList<>();

    @PrePersist
    /**
     * On create.
     */
    protected void onCreate() { createdAt = LocalDateTime.now(); }

}
