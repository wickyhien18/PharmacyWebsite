package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "refresh_tokens")

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
 * Database Entity for RefreshToken.
 * This class is used to map data and handle basic structure.
 */
public class RefreshToken {

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long id;

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    //Default value is right now
    @CreationTimestamp
    // Specifies the mapped column for a persistent property or field.
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    /**
     * On create.
     */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Checks if expired.
     *
     * @return the boolean result
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }
}
