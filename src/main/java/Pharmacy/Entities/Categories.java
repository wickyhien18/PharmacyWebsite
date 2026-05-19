package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "categories")

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
 * Database Entity for Categories.
 * This class is used to map data and handle basic structure.
 */
public class Categories {

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "category_id")
    private Long categoryId;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, name = "name", length = 255)
    private String categoryName;

    // Specifies the mapped column for a persistent property or field.
    @Column(unique = true, nullable = false, name = "slug", length = 255)
    private String categorySlug;
}
