package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "categories")

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
public class Categories {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "categoryId")
    private int categoryId;

    @Column(nullable = false, name = "categoryName")
    private String categoryName;

    private String description;

    //1 - N Relationship
    //Mapped by another Entities
    @OneToMany(mappedBy = "categories")

    //Avoid infinite loop
    @JsonIgnore
    private List<Medicines> medicines;
}
