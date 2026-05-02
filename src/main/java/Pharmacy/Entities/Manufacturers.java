package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;


//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "manufacturers")

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
public class Manufacturers {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "manufacturer_id")
    private Long manufacturerId;

    @Column(nullable = false, name = "name", length = 255)
    private String manufacturerName;

    @Column(length = 255)
    private String country;

}
